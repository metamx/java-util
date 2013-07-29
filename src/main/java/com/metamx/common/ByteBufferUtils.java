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

import com.google.common.base.Throwables;
import sun.nio.ch.FileChannelImpl;

import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;

/**
 */
public class ByteBufferUtils
{
  private static final Method unmap;

  static {
    try {
      Method unmapMethod = FileChannelImpl.class.getDeclaredMethod("unmap", MappedByteBuffer.class);
      unmapMethod.setAccessible(true);
      unmap = unmapMethod;
    }
    catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  public static void unmap(MappedByteBuffer buffer)
  {
    try {
      unmap.invoke(null, buffer);
    }
    catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }
}
