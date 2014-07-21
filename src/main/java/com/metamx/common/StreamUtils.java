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

package com.metamx.common;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.metamx.common.guava.CloseQuietly;

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
    file.getParentFile().mkdirs();
    try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
      ByteStreams.copy(is, os);
    }
    finally {
      CloseQuietly.close(is);
    }
  }

  public static void copyToFileAndClose(InputStream is, File file, long timeout) throws IOException, TimeoutException
  {
    file.getParentFile().mkdirs();
    try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
      copyWithTimeout(is, os, timeout);
    }
    finally {
      CloseQuietly.close(is);
    }
  }

  public static void copyAndClose(InputStream is, OutputStream os) throws IOException
  {
    try {
      ByteStreams.copy(is, os);
    }
    finally {
      CloseQuietly.close(is);
      CloseQuietly.close(os);
    }
  }

  public static void copyWithTimeout(InputStream is, OutputStream os, long timeout) throws IOException, TimeoutException
  {
    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    int n = 0;
    long startTime = System.currentTimeMillis();

    while (-1 != (n = is.read(buffer))) {
      if (System.currentTimeMillis() - startTime > timeout) {
        throw new TimeoutException(String.format("Copy time has exceeded %,d millis", timeout));
      }
      os.write(buffer, 0, n);
    }
  }
}
