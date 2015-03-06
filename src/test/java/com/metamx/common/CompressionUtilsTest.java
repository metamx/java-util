/*
 * Copyright 2015 Metamarkets Group Inc.
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

import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressionUtilsTest
{
  private static final String content;
  private static final byte[] expected;
  private static final byte[] gzBytes;

  static {
    final StringBuilder builder = new StringBuilder();
    try (InputStream stream = CompressionUtilsTest.class.getClassLoader().getResourceAsStream("loremipsum.txt")) {
      final Iterator<String> it = new java.util.Scanner(stream).useDelimiter(Pattern.quote("|"));
      while (it.hasNext()) {
        builder.append(it.next());
      }
    }
    catch (IOException e) {
      throw Throwables.propagate(e);
    }
    content = builder.toString();
    expected = StringUtils.toUtf8(content);

    final ByteArrayOutputStream gzByteStream = new ByteArrayOutputStream(expected.length);
    try (GZIPOutputStream outputStream = new GZIPOutputStream(gzByteStream)) {
      try (ByteArrayInputStream in = new ByteArrayInputStream(expected)) {
        ByteStreams.copy(in, outputStream);
      }
    }
    catch (IOException e) {
      throw Throwables.propagate(e);
    }
    gzBytes = gzByteStream.toByteArray();
  }

  private File testDir;
  private File testFile;

  @Before
  public void setUp() throws IOException
  {
    testDir = Files.createTempDir();
    testDir.deleteOnExit();
    testFile = Paths.get(testDir.getAbsolutePath(), "test.dat").toFile();
    testFile.deleteOnExit();
    try (OutputStream outputStream = new FileOutputStream(testFile)) {
      outputStream.write(StringUtils.toUtf8(content));
    }
    Assert.assertTrue(testFile.getParentFile().equals(testDir));
  }

  @After
  public void tearDown() throws IOException
  {
    deleteFile(testFile);
    deleteFile(testDir);
  }

  private static void deleteFile(final File file) throws IOException
  {
    if (file.exists()) {
      if (!file.delete()) {
        throw new IOException(String.format("Could not delete file [%s]", file.getAbsolutePath()));
      }
    }
  }

  public static void assertGoodDataStream(InputStream stream) throws IOException
  {
    try (final ByteArrayOutputStream bos = new ByteArrayOutputStream(expected.length)) {
      ByteStreams.copy(stream, bos);
      Assert.assertArrayEquals(expected, bos.toByteArray());
    }
  }

  @Test
  public void testGoodGzNameResolution()
  {
    Assert.assertEquals("foo", CompressionUtils.getGzBaseName("foo.gz"));
  }

  @Test(expected = IAE.class)
  public void testBadGzName()
  {
    CompressionUtils.getGzBaseName("foo");
  }


  @Test(expected = IAE.class)
  public void testBadShortGzName()
  {
    CompressionUtils.getGzBaseName(".gz");
  }

  @Test
  public void testGoodZipCompressUncompress() throws IOException
  {
    final File zipFile = File.createTempFile("compressionUtilTest", ".zip");
    zipFile.deleteOnExit();
    try {
      CompressionUtils.zip(testDir, zipFile);
      final File newDir = Files.createTempDir();
      try {
        CompressionUtils.unzip(zipFile, newDir);
        final Path newPath = Paths.get(newDir.getAbsolutePath(), testFile.getName());
        Assert.assertTrue(newPath.toFile().exists());
        try (final FileInputStream inputStream = new FileInputStream(newPath.toFile())) {
          assertGoodDataStream(inputStream);
        }
      }
      finally {
        final File[] files = newDir.listFiles();
        if (files != null) {
          for (final File file : files) {
            deleteFile(file);
          }
        }
        deleteFile(newDir);
      }
    }
    finally {
      deleteFile(zipFile);
    }
  }


  @Test
  public void testGoodZipCompressUncompressWithLocalCopy() throws IOException
  {
    final File zipFile = File.createTempFile("compressionUtilTest", ".zip");
    zipFile.deleteOnExit();
    try {
      CompressionUtils.zip(testDir, zipFile);
      final File newDir = Files.createTempDir();
      try {
        CompressionUtils.unzip(
            new ByteSource()
            {
              @Override
              public InputStream openStream() throws IOException
              {
                return new FileInputStream(zipFile);
              }
            },
            newDir,
            true
        );
        final Path newPath = Paths.get(newDir.getAbsolutePath(), testFile.getName());
        Assert.assertTrue(newPath.toFile().exists());
        try (final FileInputStream inputStream = new FileInputStream(newPath.toFile())) {
          assertGoodDataStream(inputStream);
        }
      }
      finally {
        final File[] files = newDir.listFiles();
        if (files != null) {
          for (final File file : files) {
            deleteFile(file);
          }
        }
        deleteFile(newDir);
      }
    }
    finally {
      deleteFile(zipFile);
    }
  }

  @Test
  public void testGoodGZCompressUncompressToFile() throws Exception
  {
    final File gzFile = Paths.get(testDir.getAbsolutePath(), testFile.getName() + ".gz").toFile();
    Assert.assertFalse(gzFile.exists());
    try {
      CompressionUtils.gzip(testFile, gzFile);
      Assert.assertTrue(gzFile.exists());
      try (final InputStream inputStream = new GZIPInputStream(new FileInputStream(gzFile))) {
        assertGoodDataStream(inputStream);
      }
      testFile.delete();
      Assert.assertFalse(testFile.exists());
      CompressionUtils.gunzip(gzFile, testFile);
      Assert.assertTrue(testFile.exists());
      try (final InputStream inputStream = new FileInputStream(testFile)) {
        assertGoodDataStream(inputStream);
      }
    }
    finally {
      deleteFile(gzFile);
    }
  }

  @Test
  public void testGoodZipStream() throws IOException
  {
    final File zipFile = File.createTempFile("compressionUtilTest", ".zip");
    zipFile.deleteOnExit();
    try {
      CompressionUtils.zip(testDir, new FileOutputStream(zipFile));
      final File newDir = Files.createTempDir();
      try {
        CompressionUtils.unzip(new FileInputStream(zipFile), newDir);
        final Path newPath = Paths.get(newDir.getAbsolutePath(), testFile.getName());
        Assert.assertTrue(newPath.toFile().exists());
        try (final FileInputStream inputStream = new FileInputStream(newPath.toFile())) {
          assertGoodDataStream(inputStream);
        }
      }
      finally {
        final File[] files = newDir.listFiles();
        if (files != null) {
          for (final File file : files) {
            deleteFile(file);
          }
        }
        deleteFile(newDir);
      }
    }
    finally {
      deleteFile(zipFile);
    }
  }


  @Test
  public void testGoodGzipByteSource() throws IOException
  {
    final File gzFile = Paths.get(testDir.getAbsolutePath(), testFile.getName() + ".gz").toFile();
    Assert.assertFalse(gzFile.exists());
    try {
      CompressionUtils.gzip(Files.asByteSource(testFile), Files.asByteSink(gzFile), Predicates.<Throwable>alwaysTrue());
      Assert.assertTrue(gzFile.exists());
      try (final InputStream inputStream = CompressionUtils.gzipInputStream(new FileInputStream(gzFile))) {
        assertGoodDataStream(inputStream);
      }
      if (!testFile.delete()) {
        throw new IOException(String.format("Unable to delete file [%s]", testFile.getAbsolutePath()));
      }
      Assert.assertFalse(testFile.exists());
      CompressionUtils.gunzip(Files.asByteSource(gzFile), testFile);
      Assert.assertTrue(testFile.exists());
      try (final InputStream inputStream = new FileInputStream(testFile)) {
        assertGoodDataStream(inputStream);
      }
    }
    finally {
      deleteFile(gzFile);
    }
  }

  @Test
  public void testGoodGZStream() throws IOException
  {
    final File gzFile = Paths.get(testDir.getAbsolutePath(), testFile.getName() + ".gz").toFile();
    Assert.assertFalse(gzFile.exists());
    try {
      CompressionUtils.gzip(new FileInputStream(testFile), new FileOutputStream(gzFile));
      Assert.assertTrue(gzFile.exists());
      try (final InputStream inputStream = new GZIPInputStream(new FileInputStream(gzFile))) {
        assertGoodDataStream(inputStream);
      }
      if (!testFile.delete()) {
        throw new IOException(String.format("Unable to delete file [%s]", testFile.getAbsolutePath()));
      }
      Assert.assertFalse(testFile.exists());
      CompressionUtils.gunzip(new FileInputStream(gzFile), testFile);
      Assert.assertTrue(testFile.exists());
      try (final InputStream inputStream = new FileInputStream(testFile)) {
        assertGoodDataStream(inputStream);
      }
    }
    finally {
      deleteFile(gzFile);
    }
  }

  private static class ZeroRemainingInputStream extends FilterInputStream
  {
    private final AtomicInteger pos = new AtomicInteger(0);

    protected ZeroRemainingInputStream(InputStream in)
    {
      super(in);
    }

    @Override
    public synchronized void reset() throws IOException
    {
      super.reset();
      pos.set(0);
    }

    @Override
    public int read(byte b[]) throws IOException
    {
      final int len = Math.min(b.length, gzBytes.length - pos.get() % gzBytes.length);
      pos.addAndGet(len);
      return read(b, 0, len);
    }

    @Override
    public int read() throws IOException
    {
      pos.incrementAndGet();
      return super.read();
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException
    {
      final int l = Math.min(len, gzBytes.length - pos.get() % gzBytes.length);
      pos.addAndGet(l);
      return super.read(b, off, l);
    }

    @Override
    public int available() throws IOException
    {
      return 0;
    }
  }


  @Test
  // Sanity check to make sure the test class works as expected
  public void testZeroRemainingInputStream() throws IOException
  {
    try (OutputStream outputStream = new FileOutputStream(testFile)) {
      Assert.assertEquals(
          gzBytes.length,
          ByteStreams.copy(
              new ZeroRemainingInputStream(new ByteArrayInputStream(gzBytes)),
              outputStream
          )
      );
      Assert.assertEquals(
          gzBytes.length,
          ByteStreams.copy(
              new ZeroRemainingInputStream(new ByteArrayInputStream(gzBytes)),
              outputStream
          )
      );
      Assert.assertEquals(
          gzBytes.length,
          ByteStreams.copy(
              new ZeroRemainingInputStream(new ByteArrayInputStream(gzBytes)),
              outputStream
          )
      );
    }
    Assert.assertEquals(gzBytes.length * 3, testFile.length());
    try (InputStream inputStream = new ZeroRemainingInputStream(new FileInputStream(testFile))) {
      for (int i = 0; i < 3; ++i) {
        final byte[] bytes = new byte[gzBytes.length];
        Assert.assertEquals(bytes.length, inputStream.read(bytes));
        Assert.assertArrayEquals(
            String.format("Failed on range %d", i),
            gzBytes,
            bytes
        );
      }
    }
  }

  // If this ever passes, er... fails to fail... then the bug is fixed
  @Test(expected = java.lang.AssertionError.class)
  // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7036144
  public void testGunzipBug() throws IOException
  {
    final ByteArrayOutputStream tripleGzByteStream = new ByteArrayOutputStream(gzBytes.length * 3);
    tripleGzByteStream.write(gzBytes);
    tripleGzByteStream.write(gzBytes);
    tripleGzByteStream.write(gzBytes);
    try (final InputStream inputStream = new GZIPInputStream(
        new ZeroRemainingInputStream(
            new ByteArrayInputStream(
                tripleGzByteStream.toByteArray()
            )
        )
    )) {
      try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(expected.length * 3)) {
        Assert.assertEquals(
            "Read terminated too soon (bug 7036144)",
            expected.length * 3,
            ByteStreams.copy(inputStream, outputStream)
        );
        final byte[] found = outputStream.toByteArray();
        Assert.assertEquals(expected.length * 3, found.length);
        Assert.assertArrayEquals(expected, Arrays.copyOfRange(found, expected.length * 0, expected.length * 1));
        Assert.assertArrayEquals(expected, Arrays.copyOfRange(found, expected.length * 1, expected.length * 2));
        Assert.assertArrayEquals(expected, Arrays.copyOfRange(found, expected.length * 2, expected.length * 3));
      }
    }
  }

  @Test
  // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7036144
  public void testGunzipBugworkarround() throws IOException
  {
    testFile.delete();
    Assert.assertFalse(testFile.exists());

    final ByteArrayOutputStream tripleGzByteStream = new ByteArrayOutputStream(gzBytes.length * 3);
    tripleGzByteStream.write(gzBytes);
    tripleGzByteStream.write(gzBytes);
    tripleGzByteStream.write(gzBytes);

    final ByteSource inputStreamFactory = new ByteSource()
    {
      @Override
      public InputStream openStream() throws IOException
      {
        return new ZeroRemainingInputStream(new ByteArrayInputStream(tripleGzByteStream.toByteArray()));
      }
    };

    Assert.assertEquals((long) (expected.length * 3), CompressionUtils.gunzip(inputStreamFactory, testFile).size());

    try (final InputStream inputStream = new FileInputStream(testFile)) {
      try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(expected.length * 3)) {
        Assert.assertEquals(
            "Read terminated too soon (7036144)",
            expected.length * 3,
            ByteStreams.copy(inputStream, outputStream)
        );
        final byte[] found = outputStream.toByteArray();
        Assert.assertEquals(expected.length * 3, found.length);
        Assert.assertArrayEquals(expected, Arrays.copyOfRange(found, expected.length * 0, expected.length * 1));
        Assert.assertArrayEquals(expected, Arrays.copyOfRange(found, expected.length * 1, expected.length * 2));
        Assert.assertArrayEquals(expected, Arrays.copyOfRange(found, expected.length * 2, expected.length * 3));
      }
    }
  }


  @Test
  // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7036144
  public void testGunzipBugStreamWorkarround() throws IOException
  {

    final ByteArrayOutputStream tripleGzByteStream = new ByteArrayOutputStream(gzBytes.length * 3);
    tripleGzByteStream.write(gzBytes);
    tripleGzByteStream.write(gzBytes);
    tripleGzByteStream.write(gzBytes);

    try (ByteArrayOutputStream bos = new ByteArrayOutputStream(expected.length * 3)) {
      Assert.assertEquals(
          expected.length * 3,
          CompressionUtils.gunzip(
              new ZeroRemainingInputStream(
                  new ByteArrayInputStream(tripleGzByteStream.toByteArray())
              ), bos
          )
      );
      final byte[] found = bos.toByteArray();
      Assert.assertEquals(expected.length * 3, found.length);
      Assert.assertArrayEquals(expected, Arrays.copyOfRange(found, expected.length * 0, expected.length * 1));
      Assert.assertArrayEquals(expected, Arrays.copyOfRange(found, expected.length * 1, expected.length * 2));
      Assert.assertArrayEquals(expected, Arrays.copyOfRange(found, expected.length * 2, expected.length * 3));
    }
  }

  @Test
  public void testZipName() throws IOException
  {
    final File tmpDir = Files.createTempDir();
    tmpDir.deleteOnExit();
    final File file = Paths.get(tmpDir.getAbsolutePath(), ".zip").toFile();
    final Path unzipPath = Paths.get(tmpDir.getAbsolutePath(), "test.dat");
    file.delete();
    Assert.assertFalse(file.exists());
    Assert.assertFalse(unzipPath.toFile().exists());
    try {
      CompressionUtils.zip(testDir, file);
      Assert.assertTrue(file.exists());
      CompressionUtils.unzip(file, tmpDir);
      Assert.assertTrue(unzipPath.toFile().exists());
      try (final FileInputStream inputStream = new FileInputStream(unzipPath.toFile())) {
        assertGoodDataStream(inputStream);
      }
    }
    finally {
      file.delete();
      unzipPath.toFile().delete();
      tmpDir.delete();
    }
  }

  @Test
  public void testNewFileDoesntCreateFile()
  {
    final File tmpFile = new File(testDir, "fofooofodshfudhfwdjkfwf.dat");
    Assert.assertFalse(tmpFile.exists());
  }

  @Test
  public void testGoodGzipName()
  {
    Assert.assertEquals("foo", CompressionUtils.getGzBaseName("foo.gz"));
  }

  @Test
  public void testGoodGzipNameWithPath()
  {
    Assert.assertEquals("foo", CompressionUtils.getGzBaseName("/tar/ball/baz/bock/foo.gz"));
  }

  @Test(expected = IAE.class)
  public void testBadShortName()
  {
    CompressionUtils.getGzBaseName(".gz");
  }

  @Test(expected = IAE.class)
  public void testBadName()
  {
    CompressionUtils.getGzBaseName("BANANAS");
  }

  @Test(expected = IAE.class)
  public void testBadNameWithPath()
  {
    CompressionUtils.getGzBaseName("/foo/big/.gz");
  }
}
