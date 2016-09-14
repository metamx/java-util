/*
 * Copyright 2016 Metamarkets Group Inc.
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

package com.metamx.common;

import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class ByteBufferUtilsTest
{
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void testUnmapDoesntCrashJVM() throws Exception
  {
    final File file = temporaryFolder.newFile("some_mmap_file");
    try (final OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
      final byte[] data = new byte[4096];
      Arrays.fill(data, (byte) 0x5A);
      os.write(data);
    }
    final MappedByteBuffer mappedByteBuffer;
    try (final FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.READ)) {
      mappedByteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, 4096);
    }
    Assert.assertEquals((byte) 0x5A, mappedByteBuffer.get(0));
    ByteBufferUtils.unmap(mappedByteBuffer);
    ByteBufferUtils.unmap(mappedByteBuffer);
  }

  @Test
  public void testFreeDoesntCrashJVM() throws Exception
  {
    final ByteBuffer directBuffer = ByteBuffer.allocateDirect(4096);
    ByteBufferUtils.free(directBuffer);
    ByteBufferUtils.free(directBuffer);

    final ByteBuffer heapBuffer = ByteBuffer.allocate(4096);
    ByteBufferUtils.free(heapBuffer);
  }
}
