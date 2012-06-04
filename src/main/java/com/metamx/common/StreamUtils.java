package com.metamx.common;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeoutException;

/**
 */
public class StreamUtils
{
  // The default buffer size to use (from IOUtils)
  private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

  public static void copyToFileAndClose(InputStream is, File file) throws IOException
  {
    OutputStream os = null;
    try {
      file.getParentFile().mkdirs();

      os = new BufferedOutputStream(new FileOutputStream(file));
      ByteStreams.copy(is, os);
    }
    finally {
      Closeables.closeQuietly(is);
      Closeables.closeQuietly(os);
    }
  }

  public static void copyToFileAndClose(InputStream is, File file, long timeout) throws IOException, TimeoutException
  {
    OutputStream os = null;
    
    try {
      file.getParentFile().mkdirs();

      os = new BufferedOutputStream(new FileOutputStream(file));
      copyWithTimeout(is, os, timeout);
    }
    finally {
      Closeables.closeQuietly(is);
      Closeables.closeQuietly(os);
    }
  }

  public static void copyAndClose(InputStream is, OutputStream os) throws IOException
  {
    try {
      ByteStreams.copy(is, os);
    }
    finally {
      Closeables.closeQuietly(is);
      Closeables.closeQuietly(os);
    }
  }

  public static void copyWithTimeout(InputStream is, OutputStream os, long timeout) throws IOException, TimeoutException
  {
    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    long count = 0;
    int n = 0;
    long startTime = System.currentTimeMillis();

    while (-1 != (n = is.read(buffer))) {
      if (System.currentTimeMillis() - startTime > timeout) {
        throw new TimeoutException(String.format("Copy time has exceeded %,d millis", timeout));
      }
      os.write(buffer, 0, n);
      count += n;
    }
  }
}
