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

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.metamx.common.IAE;
import com.metamx.common.ISE;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class that concatenates files together into configurable sized chunks, works in conjunction
 * with the SmooshedFileMapper to provide access to the individual files.
 * <p/>
 * It does not split input files among separate output files, instead the various "chunk" files will
 * be varying sizes and it is not possible to add a file of size greater than Integer.MAX_VALUE
 */
public class FileSmoosher implements Closeable
{
  private static final String FILE_EXTENSION = "smoosh";
  private static final Joiner joiner = Joiner.on(",");

  private final File baseDir;
  private final int maxChunkSize;

  private final List<File> outFiles = Lists.newArrayList();
  private final Map<String, Metadata> internalFiles = Maps.newTreeMap();

  private Outer currOut = null;

  public FileSmoosher(
      File baseDir
  )
  {
    this(baseDir, Integer.MAX_VALUE);
  }

  public FileSmoosher(
      File baseDir,
      int maxChunkSize
  )
  {
    this.baseDir = baseDir;
    this.maxChunkSize = maxChunkSize;

    Preconditions.checkArgument(maxChunkSize > 0, "maxChunkSize must be a positive value.");
  }

  private FileSmoosher(
      File baseDir,
      int maxChunkSize,
      List<File> outFiles,
      Map<String, Metadata> internalFiles
  )
  {
    this.baseDir = baseDir;
    this.maxChunkSize = maxChunkSize;
    this.outFiles.addAll(outFiles);
    this.internalFiles.putAll(internalFiles);
  }

  public Set<String> getInternalFilenames()
  {
    return internalFiles.keySet();
  }

  public void add(File fileToAdd) throws IOException
  {
    add(fileToAdd.getName(), Files.map(fileToAdd));
  }

  public void add(String name, File fileToAdd) throws IOException
  {
    add(name, Files.map(fileToAdd));
  }

  public void add(String name, ByteBuffer bufferToAdd) throws IOException
  {
    if (name.contains(",")) {
      throw new IAE("Cannot have a comma in the name of a file, got[%s].", name);
    }

    if (internalFiles.get(name) != null) {
      throw new IAE("Cannot add files of the same name, already have [%s]", name);
    }

    final long size = bufferToAdd.remaining();
    if (size > maxChunkSize) {
      throw new IAE("Asked to add buffer[%,d] larger than configured max[%,d]", size, maxChunkSize);
    }
    if (currOut == null) {
      currOut = getNewCurrOut();
    }
    if (currOut.bytesLeft() < size) {
      Closeables.close(currOut, false);
      currOut = getNewCurrOut();
    }

    int startOffset = currOut.getCurrOffset();
    currOut.write(bufferToAdd);
    int endOffset = currOut.getCurrOffset();

    internalFiles.put(name, new Metadata(currOut.getFileNum(), startOffset, endOffset));
  }

  @Override
  public void close() throws IOException
  {
    Closeables.close(currOut, false);

    File metaFile = metaFile(baseDir);

    Writer out = null;
    try {
      out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(metaFile), Charsets.UTF_8));
      out.write(String.format("v1,%d,%d", maxChunkSize, outFiles.size()));
      out.write("\n");

      for (Map.Entry<String, Metadata> entry : internalFiles.entrySet()) {
        final Metadata metadata = entry.getValue();
        out.write(
            joiner.join(
                entry.getKey(),
                metadata.getFileNum(),
                metadata.getStartOffset(),
                metadata.getEndOffset()
            )
        );
        out.write("\n");
      }
    }
    finally {
      Closeables.close(out, false);
    }
  }

  private Outer getNewCurrOut() throws FileNotFoundException
  {
    final int fileNum = outFiles.size();
    File outFile = makeChunkFile(baseDir, fileNum);
    outFiles.add(outFile);
    return new Outer(fileNum, new BufferedOutputStream(new FileOutputStream(outFile)), maxChunkSize);
  }

  static File metaFile(File baseDir)
  {
    return new File(baseDir, String.format("meta.%s", FILE_EXTENSION));
  }

  static File makeChunkFile(File baseDir, int i)
  {
    return new File(baseDir, String.format("%05d.%s", i, FILE_EXTENSION));
  }

  private static class Outer implements Closeable
  {
    private final int fileNum;
    private final OutputStream out;
    private final int maxLength;

    private int currOffset = 0;

    Outer(int fileNum, OutputStream out, int maxLength)
    {
      this.fileNum = fileNum;
      this.out = out;
      this.maxLength = maxLength;
    }

    public int getFileNum()
    {
      return fileNum;
    }

    public int getCurrOffset()
    {
      return currOffset;
    }

    public int bytesLeft()
    {
      return maxLength - currOffset;
    }

    public void write(ByteBuffer buffer) throws IOException
    {
      WritableByteChannel channel = Channels.newChannel(out);
      long numBytesWritten = channel.write(buffer);

      if (numBytesWritten > bytesLeft()) {
        throw new ISE("Wrote more bytes[%,d] than available[%,d]. Don't do that.", numBytesWritten, bytesLeft());
      }

      currOffset += numBytesWritten;
    }

    public void write(InputStream in) throws IOException
    {
      long numBytesWritten = ByteStreams.copy(in, out);

      if (numBytesWritten > bytesLeft()) {
        throw new ISE("Wrote more bytes[%,d] than available[%,d]. Don't do that.", numBytesWritten, bytesLeft());
      }

      currOffset += numBytesWritten;
    }

    @Override
    public void close() throws IOException
    {
      out.close();
    }
  }
}
