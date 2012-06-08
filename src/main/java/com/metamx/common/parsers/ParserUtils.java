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
import com.google.common.collect.Sets;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

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

  public static Function<String, DateTime> createTimestampParser(final String format)
  {
    if (format.equalsIgnoreCase("iso")) {
      return new Function<String, DateTime>()
      {
        @Override
        public DateTime apply(String input)
        {
          Preconditions.checkArgument(input != null && !input.isEmpty(), "null timestamp");
          return new DateTime(input);
        }
      };
    } else if (format.equalsIgnoreCase("posix")) {
      return new Function<String, DateTime>()
      {
        @Override
        public DateTime apply(String input)
        {
          Preconditions.checkArgument(input != null && !input.isEmpty(), "null timestamp");
          return new DateTime(Long.parseLong(input) * 1000);
        }
      };
    } else {
      try {
        final DateTimeFormatter formatter = DateTimeFormat.forPattern(format);
        return new Function<String, DateTime>()
        {
          @Override
          public DateTime apply(String input)
          {
            Preconditions.checkArgument(input != null && !input.isEmpty(), "null timestamp");
            return formatter.parseDateTime(input);
          }
        };
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(String.format("Unknown timestamp format [%s]", format));
      }
    }

  }

  public static Set<String> findDuplicates(Iterable<String> fieldNames)
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
}
