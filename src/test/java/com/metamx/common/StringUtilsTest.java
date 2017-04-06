/*
 * Copyright 2014 Metamarkets Group Inc.
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
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 *
 */
public class StringUtilsTest
{
  // copied from https://github.com/druid-io/druid/pull/2612
  public final static String[] TEST_STRINGS = new String[]{
      "peach", "péché", "pêche", "sin", "",
      "☃", "C", "c", "Ç", "ç", "G", "g", "Ğ", "ğ", "I", "ı", "İ", "i",
      "O", "o", "Ö", "ö", "S", "s", "Ş", "ş", "U", "u", "Ü", "ü", "ä",
      "\uD841\uDF0E",
      "\uD841\uDF31",
      "\uD844\uDC5C",
      "\uD84F\uDCB7",
      "\uD860\uDEE2",
      "\uD867\uDD98",
      "\u006E\u0303",
      "\u006E",
      "\uFB00",
      "\u0066\u0066",
      "Å",
      "\u00C5",
      "\u212B"
  };

  @Test
  public void binaryLengthAsUTF8Test() throws UnsupportedEncodingException
  {
    for (String string : TEST_STRINGS) {
      Assert.assertEquals(StringUtils.toUtf8(string).length, StringUtils.binaryLengthAsUTF8(string));
    }
  }

  @Test
  public void binaryLengthAsUTF8InvalidTest() throws UnsupportedEncodingException
  {
    String invalid = "\uD841";  // high only
    Assert.assertEquals(1, StringUtils.toUtf8(invalid).length);
    Assert.assertEquals(4, StringUtils.binaryLengthAsUTF8(invalid));

    invalid = "\uD841\uD841";  // high + high
    Assert.assertEquals(2, StringUtils.toUtf8(invalid).length);
    Assert.assertEquals(4, StringUtils.binaryLengthAsUTF8(invalid));

    invalid = "\uD841\u0050";  // high + char
    Assert.assertEquals(2, StringUtils.toUtf8(invalid).length);
    Assert.assertEquals(4, StringUtils.binaryLengthAsUTF8(invalid));

    invalid = "\uDEE2\uD841";  // low + high
    Assert.assertEquals(2, StringUtils.toUtf8(invalid).length);
    Assert.assertEquals(4, StringUtils.binaryLengthAsUTF8(invalid));
  }

  @Test
  public void fromUtf8ConversionTest() throws UnsupportedEncodingException
  {
    byte[] bytes = new byte[]{'a', 'b', 'c', 'd'};
    Assert.assertEquals("abcd", StringUtils.fromUtf8(bytes));

    String abcd = "abcd";
    Assert.assertEquals(abcd, StringUtils.fromUtf8(abcd.getBytes(StringUtils.UTF8_STRING)));
  }

  @Test
  public void toUtf8ConversionTest()
  {
    byte[] bytes = new byte[]{'a', 'b', 'c', 'd'};
    byte[] strBytes = StringUtils.toUtf8("abcd");
    for (int i = 0; i < bytes.length; ++i) {
      Assert.assertEquals(bytes[i], strBytes[i]);
    }
  }

  @Test
  public void fromUtf8ByteBufferHeap()
  {
    ByteBuffer bytes = ByteBuffer.wrap(new byte[]{'a', 'b', 'c', 'd'});
    Assert.assertEquals("abcd", StringUtils.fromUtf8(bytes, 4));
    bytes.rewind();
    Assert.assertEquals("abcd", StringUtils.fromUtf8(bytes));
  }

  @Test
  public void testMiddleOfByteArrayConversion()
  {
    ByteBuffer bytes = ByteBuffer.wrap(new byte[]{'a', 'b', 'c', 'd'});
    bytes.position(1).limit(3);
    Assert.assertEquals("bc", StringUtils.fromUtf8(bytes, 2));
    bytes.position(1);
    Assert.assertEquals("bc", StringUtils.fromUtf8(bytes));
  }


  @Test(expected = java.nio.BufferUnderflowException.class)
  public void testOutOfBounds()
  {
    ByteBuffer bytes = ByteBuffer.wrap(new byte[]{'a', 'b', 'c', 'd'});
    bytes.position(1).limit(3);
    StringUtils.fromUtf8(bytes, 3);
  }

  @Test(expected = java.lang.NullPointerException.class)
  public void testNullPointerByteBuffer()
  {
    StringUtils.fromUtf8((ByteBuffer) null);
  }

  @Test(expected = java.lang.NullPointerException.class)
  public void testNullPointerByteArray()
  {
    StringUtils.fromUtf8((byte[]) null);
  }

  @Test
  public void fromUtf8ByteBufferDirect()
  {
    ByteBuffer bytes = ByteBuffer.allocateDirect(4);
    bytes.put(new byte[]{'a', 'b', 'c', 'd'});
    bytes.rewind();
    Assert.assertEquals("abcd", StringUtils.fromUtf8(bytes, 4));
    bytes.rewind();
    Assert.assertEquals("abcd", StringUtils.fromUtf8(bytes));
  }

  @Test
  public void testCharsetShowsUpAsDeprecated()
  {
    // Not actually a runnable test, just checking the IDE
    Assert.assertNotNull(StringUtils.UTF8_CHARSET);
  }

  @Test
  public void testSafeFormat()
  {
    Assert.assertEquals("test%d; format", StringUtils.safeFormat("test%d", "format"));
    Assert.assertEquals("test%s%s; format", StringUtils.safeFormat("test%s%s", "format"));
  }
}
