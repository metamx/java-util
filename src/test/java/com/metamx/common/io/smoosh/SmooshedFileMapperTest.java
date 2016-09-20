/*
 * Copyright 2011,2012 Metamarkets Group Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.metamx.common.io.smoosh;

import com.google.common.io.Files;
import com.google.common.primitives.Ints;
import com.metamx.common.ISE;
import com.metamx.common.guava.CloseQuietly;
import junit.framework.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 */
public class SmooshedFileMapperTest
{
  @Test
  public void testSanity() throws Exception
  {
    File baseDir = Files.createTempDir();
    baseDir.deleteOnExit();

    try {
      FileSmoosher smoosher = new FileSmoosher(baseDir, 21);
      for (int i = 0; i < 20; ++i) {
        File tmpFile = File.createTempFile(String.format("smoosh-%s", i), ".bin");
        tmpFile.deleteOnExit();
        Files.write(Ints.toByteArray(i), tmpFile);
        smoosher.add(String.format("%d", i), tmpFile);
        tmpFile.delete();
      }
      smoosher.close();

      File[] files = baseDir.listFiles();
      Arrays.sort(files);

      Assert.assertEquals(5, files.length); // 4 smooshed files and 1 meta file
      for (int i = 0; i < 4; ++i) {
        Assert.assertEquals(FileSmoosher.makeChunkFile(baseDir, i), files[i]);
      }
      Assert.assertEquals(FileSmoosher.metaFile(baseDir), files[files.length - 1]);

      SmooshedFileMapper mapper = SmooshedFileMapper.load(baseDir);
      for (int i = 0; i < 20; ++i) {
        ByteBuffer buf = mapper.mapFile(String.format("%d", i));
        Assert.assertEquals(0, buf.position());
        Assert.assertEquals(4, buf.remaining());
        Assert.assertEquals(4, buf.capacity());
        Assert.assertEquals(i, buf.getInt());
      }
      mapper.close();
    }
    finally {
      for (File file : baseDir.listFiles()) {
        file.delete();
      }
    }
  }

  @Test
  public void testBehaviorWhenReportedSizesLargeAndExceptionIgnored() throws Exception
  {
    File baseDir = Files.createTempDir();
    baseDir.deleteOnExit();

    try {
      FileSmoosher smoosher = new FileSmoosher(baseDir, 21);
      for (int i = 0; i < 20; ++i) {
        final SmooshedWriter writer = smoosher.addWithSmooshedWriter(String.format("%d", i), 7);
        writer.write(ByteBuffer.wrap(Ints.toByteArray(i)));
        CloseQuietly.close(writer);
      }
      smoosher.close();

      File[] files = baseDir.listFiles();
      Arrays.sort(files);

      Assert.assertEquals(6, files.length); // 4 smoosh files and 1 meta file
      for (int i = 0; i < 4; ++i) {
        Assert.assertEquals(FileSmoosher.makeChunkFile(baseDir, i), files[i]);
      }
      Assert.assertEquals(FileSmoosher.metaFile(baseDir), files[files.length - 1]);

      SmooshedFileMapper mapper = SmooshedFileMapper.load(baseDir);
      for (int i = 0; i < 20; ++i) {
        ByteBuffer buf = mapper.mapFile(String.format("%d", i));
        Assert.assertEquals(0, buf.position());
        Assert.assertEquals(4, buf.remaining());
        Assert.assertEquals(4, buf.capacity());
        Assert.assertEquals(i, buf.getInt());
      }
      mapper.close();
    }
    finally {
      for (File file : baseDir.listFiles()) {
        file.delete();
      }
    }
  }

  @Test
  public void testBehaviorWhenReportedSizesSmall() throws Exception
  {
    File baseDir = Files.createTempDir();
    baseDir.deleteOnExit();

    try {
      FileSmoosher smoosher = new FileSmoosher(baseDir, 21);
      final SmooshedWriter writer = smoosher.addWithSmooshedWriter("1", 2);
      boolean exceptionThrown = false;
      try {
        writer.write(ByteBuffer.wrap(Ints.toByteArray(1)));
      }
      catch (ISE e) {
        Assert.assertTrue(e.getMessage().contains("Liar!!!"));
        exceptionThrown = true;
      }

      Assert.assertTrue(exceptionThrown);
      File[] files = baseDir.listFiles();
      Assert.assertEquals(1, files.length);
      Assert.assertEquals(0, files[0].length());
    }
    finally {
      for (File file : baseDir.listFiles()) {
        file.delete();
      }
    }
  }

  @Test
  public void testDeterministicFileUnmapping() throws IOException
  {
    File baseDir = Files.createTempDir();
    baseDir.deleteOnExit();

    int fileSize = 1 << 20; // 1 MB
    try {
      long totalMemoryUsedBeforeAddingFile = totalMemoryUsedByDirectAndMappedBuffers();
      FileSmoosher smoosher = new FileSmoosher(baseDir);
      File dataFile = createTempFileOfSize("data", "bin", fileSize);
      smoosher.add(dataFile);
      // In case smoosher maps some own files internally (though currently it is not), let it unmap them
      smoosher.close();
      long totalMemoryUsedAfterAddingFile = totalMemoryUsedByDirectAndMappedBuffers();
      // Assert no hanging file mappings left by either smoosher or smoosher.add(file)
      Assert.assertEquals(totalMemoryUsedBeforeAddingFile, totalMemoryUsedAfterAddingFile);
    }
    finally {
      for (File file : baseDir.listFiles()) {
        file.delete();
      }
    }
  }

  private static File createTempFileOfSize(String prefix, String suffix, int fileSize) throws IOException
  {
    File dataFile = File.createTempFile(prefix, suffix);
    dataFile.deleteOnExit();
    try (RandomAccessFile raf = new RandomAccessFile(dataFile, "rw")) {
      raf.setLength(fileSize);
    }
    return dataFile;
  }

  private static long totalMemoryUsedByDirectAndMappedBuffers()
  {
    long totalMemoryUsed = 0L;
    List<BufferPoolMXBean> pools = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);
    for (BufferPoolMXBean pool : pools) {
      totalMemoryUsed += pool.getMemoryUsed();
    }
    return totalMemoryUsed;
  }
}
