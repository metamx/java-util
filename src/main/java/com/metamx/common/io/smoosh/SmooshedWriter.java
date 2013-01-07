package com.metamx.common.io.smoosh;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.WritableByteChannel;

/**
 */
public interface SmooshedWriter extends Closeable, WritableByteChannel
{
  public int write(InputStream in) throws IOException;
}
