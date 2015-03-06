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

import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

public class FileUtils
{
  /**
   * Useful for retry functionality that doesn't want to stop Throwables, but does want to retry on Exceptions
   */
  public static final Predicate<Throwable> IS_EXCEPTION = new Predicate<Throwable>()
  {
    @Override
    public boolean apply(Throwable input)
    {
      return input instanceof Exception;
    }
  };
  /**
   * Copy input byte source to outFile. If outFile exists, it is attempted to be deleted.
   *
   * @param byteSource  Supplier for an input stream that is to be copied. The resulting stream is closed each iteration
   * @param outFile     Where the file should be written to.
   * @param shouldRetry Predicate indicating if an error is recoverable and should be retried.
   * @param maxAttempts The maximum number of assumed recoverable attempts to try before completely failing.
   *
   * @throws java.lang.RuntimeException wrapping the inner exception on failure.
   */
  public static FileCopyResult retryCopy(
      final ByteSource byteSource,
      final File outFile,
      final Predicate<Throwable> shouldRetry,
      final int maxAttempts
  )
  {
    try {
      StreamUtils.retryCopy(
          byteSource,
          Files.asByteSink(outFile),
          shouldRetry,
          maxAttempts
      );
      return new FileCopyResult(outFile);
    }
    catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  /**
   * Keeps results of a file copy, including children and total size of the resultant files.
   * This class is NOT thread safe.
   * Child size is eagerly calculated and any modifications to the file after the child is added are not accounted for.
   * As such, this result should be considered immutable, even though it has no way to force that property on the files.
   */
  public static class FileCopyResult
  {
    private final Collection<File> files = Lists.newArrayList();
    private long size = 0l;

    public Collection<File> getFiles()
    {
      return ImmutableList.copyOf(files);
    }

    // Only works for immutable children contents
    public long size()
    {
      return size;
    }

    public FileCopyResult(File... files)
    {
      this(files == null ? ImmutableList.<File>of() : Arrays.asList(files));
    }

    public FileCopyResult(Collection<File> files)
    {
      this.addSizedFiles(files);
    }

    protected void addSizedFiles(Collection<File> files)
    {
      if (files == null || files.isEmpty()) {
        return;
      }
      long size = 0l;
      for (File file : files) {
        size += file.length();
      }
      this.files.addAll(files);
      this.size += size;
    }

    public void addFiles(Collection<File> files)
    {
      this.addSizedFiles(files);
    }

    public void addFile(File file)
    {
      this.addFiles(ImmutableList.of(file));
    }
  }
}
