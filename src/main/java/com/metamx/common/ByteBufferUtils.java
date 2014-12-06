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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

/**
 */
public class ByteBufferUtils
{
  private static final Method unmap;

  private static final Method getCleaner;
  private static final Method clean;


  static {
    try {
      Method unmapMethod = Class.forName("sun.nio.ch.FileChannelImpl")
                                .getDeclaredMethod("unmap", MappedByteBuffer.class);
      unmapMethod.setAccessible(true);
      unmap = unmapMethod;
    }
    catch (Exception e) {
      throw new UOE(e, "Exception thrown while trying to find unmap method on MappedByteBuffer, "
                       + "this method must exist in your VM in order for this to work");
    }
  }

  static {
    try {
      getCleaner = Class.forName("java.nio.DirectByteBuffer").getDeclaredMethod("cleaner");
      getCleaner.setAccessible(true);
      clean = Class.forName("sun.misc.Cleaner").getDeclaredMethod("clean");
      clean.setAccessible(true);
    } catch(ClassNotFoundException | NoSuchMethodException e) {
      throw new UOE("Exception thrown while trying to access ByteBuffer clean method.");
    }
  }

  /**
   * Releases memory held by the given direct ByteBuffer
   *
   * @param buffer buffer to free
   */
  public static void free(ByteBuffer buffer) {
    try {
      clean.invoke(getCleaner.invoke(buffer));
    } catch(IllegalAccessException | InvocationTargetException e) {
      throw Throwables.propagate(e);
    }
  }


  /**
   * Un-maps the given memory mapped file
   *
   * @param buffer buffer
   */
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
