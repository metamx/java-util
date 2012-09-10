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
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
    Map<String, DateTimeZone> timezones = new HashMap<String, DateTimeZone>();
    timezones.put("PST", DateTimeZone.forOffsetHours(-8));
    timezones.put("CST", DateTimeZone.forOffsetHours(-6));
    timezones.put("EST", DateTimeZone.forOffsetHours(-5));
    return ParserUtils.createTimestampParser(format, timezones);
  }

  public static Function<String, DateTime> createTimestampParser(final String format, final Map<String, DateTimeZone> timezones)
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
    } else if(format.equalsIgnoreCase("millis")) {
      return new Function<String, DateTime>() {
        @Override
        public DateTime apply(String input) {
          Preconditions.checkArgument(input != null && !input.isEmpty(), "null timestamp");
          return new DateTime(Long.parseLong(input));
        }
      };
    } else {
      try {
        final int timeZoneIndex = format.indexOf('z');
        if(timeZoneIndex == -1) {
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
        } else {
          return new Function<String, DateTime>()
          {
            @Override
            public DateTime apply(String input)
            {
              Preconditions.checkArgument(input != null && !input.isEmpty(), "null timestamp");
              return buildTimestampParser(format, timezones).toFormatter().parseDateTime(input);
            }
          };
        }
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(String.format("Unknown timestamp format [%s]", format));
      }
    }
  }

  private static DateTimeFormatterBuilder buildTimestampParser(String format, Map<String, DateTimeZone> timezones) {
    DateTimeFormatterBuilder formatBuilder = new DateTimeFormatterBuilder();
    boolean insideLiteral = false;
    int parseablePatternStart = 0;
    for(int i = 0; i < format.length(); i++) {
      char f = format.charAt(i);
      if(f == '\'' && !insideLiteral)
        insideLiteral = true;
      else if(f == '\'' && insideLiteral)
        insideLiteral = false;
      if(f == 'z' && !insideLiteral) {
        String test = format.substring(parseablePatternStart, i);
        formatBuilder.append(DateTimeFormat.forPattern(format.substring(parseablePatternStart,i)))
                     .appendTimeZoneShortName(timezones);
        parseablePatternStart = i+1;
      }
    }
    String test = format.substring(parseablePatternStart);
    formatBuilder.append(DateTimeFormat.forPattern(format.substring(parseablePatternStart)));
    return formatBuilder;
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
