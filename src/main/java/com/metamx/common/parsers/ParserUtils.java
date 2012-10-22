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

package com.metamx.common.parsers;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.metamx.common.exception.FormattedException;
import com.metamx.common.exception.SubErrorHolder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserUtils
{
  public static final Function<String, String> nullEmptyStringFunction = new Function<String, String>()
  {
    @Override
    public String apply(String input)
    {
      if (input == null || input.isEmpty()) {
        return null;
      }
      return input;
    }
  };

  public static ArrayList<String> generateFieldNames(int length)
  {
    ArrayList<String> names = new ArrayList<String>(length);
    for (int i = 0; i < length; ++i) {
      names.add("column_" + (i + 1));
    }
    return names;
  }

  /**
   * Factored timestamp parsing into its own Parser class, but leaving this here
   * for compatibility
   * @param format
   * @return
   */
  public static Function<String, DateTime> createTimestampParser(final String format) {
    return TimestampParser.createTimestampParser(format);
  }

  private static Set<String> findDuplicates(Iterable<String> fieldNames)
  {
    Set<String> duplicates = Sets.newHashSet();
    Set<String> uniqueNames = Sets.newHashSet();
    Iterator<String> iter = fieldNames.iterator();

    while (iter.hasNext()) {
      String next = iter.next().toLowerCase();
      if (uniqueNames.contains(next)) {
        duplicates.add(next);
      }
      uniqueNames.add(next);
    }

    return duplicates;
  }

  public static void validateFields(Iterable<String> fieldNames) throws FormattedException
  {
    Set<String> duplicates = findDuplicates(fieldNames);
    if (!duplicates.isEmpty()) {
      throw new FormattedException.Builder()
          .withErrorCode(FormattedException.ErrorCode.UNPARSABLE_HEADER)
          .withDetails(
              new SubErrorHolder(
                  FormattedException.SubErrorCode.DUPLICATE_KEY,
                  fieldNames,
                  duplicates
              ).get()
          )
          .withMessage(String.format("Duplicate entries found: %s", duplicates.toString()))
          .build();
    }
  }

  public static String stripQuotes(String input) {
    input = input.trim();
    if(input.charAt(0) == '\"' && input.charAt(input.length()-1) == '\"')
      input = input.substring(1, input.length() - 1).trim();
    return input;
  }
}
