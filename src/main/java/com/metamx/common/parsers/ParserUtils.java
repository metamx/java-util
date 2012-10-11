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
import com.google.common.collect.Sets;
import com.metamx.common.exception.FormattedException;
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

  public static Function<String, DateTime> createTimestampParser(final String format)
  {
    Map<String, DateTimeZone> timezones = new HashMap<String, DateTimeZone>();
    try {
      InputStream fileInput = ParserUtils.class.getResourceAsStream("/timezone.properties");
      Properties properties = new Properties();
      properties.load(fileInput);

      Enumeration enuKeys = properties.keys();
      while (enuKeys.hasMoreElements()) {
        String zone = (String) enuKeys.nextElement();
        float offset = Float.parseFloat(properties.getProperty(zone));
        int hours = (int) Math.floor(offset);
        int minutes = (int) (60 * (offset - hours));
        timezones.put(zone, DateTimeZone.forOffsetHoursMinutes(hours, minutes));
      }
    }
    catch (FileNotFoundException e) {
      throw new FormattedException.Builder()
          .withErrorCode(FormattedException.ErrorCode.SERVER_ERROR)
          .withMessage("Could not find timezone configuration file - timezone.properties - in resource folder")
          .build();
    }
    catch (IOException e) {
      throw new FormattedException.Builder()
          .withErrorCode(FormattedException.ErrorCode.SERVER_ERROR)
          .withMessage("Could not read timezone configuration file - timezone.properties - in resource folder")
          .build();
    }
    return ParserUtils.createTimestampParser(format, timezones);
  }

  public static Function<String, DateTime> createTimestampParser(
      final String format,
      final Map<String, DateTimeZone> timezones
  )
  {
    if(format.equalsIgnoreCase("auto")) {
      // Could be iso or millis
      return new Function<String, DateTime>()
      {
        @Override
        public DateTime apply(String input)
        {
          Preconditions.checkArgument(input != null && !input.isEmpty(), "null timestamp");
          for(int i = 0 ; i < input.length() ; i++) {
            if(input.charAt(i) < '0' || input.charAt(i) > '9') {
              return new DateTime(stripQuotes(input));
            }
          }

          return new DateTime(Long.parseLong(input));
        }
      };
    } else if (format.equalsIgnoreCase("iso")) {
      return new Function<String, DateTime>()
      {
        @Override
        public DateTime apply(String input)
        {
          Preconditions.checkArgument(input != null && !input.isEmpty(), "null timestamp");
          return new DateTime(stripQuotes(input));
        }
      };
    } else if (format.equalsIgnoreCase("posix")) {
      return new Function<String, DateTime>()
      {
        @Override
        public DateTime apply(String input)
        {
          Preconditions.checkArgument(input != null && !input.isEmpty(), "null timestamp");
          return new DateTime(Long.parseLong(stripQuotes(input)) * 1000);
        }
      };
    } else if (format.equalsIgnoreCase("millis")) {
      return new Function<String, DateTime>()
      {
        @Override
        public DateTime apply(String input)
        {
          Preconditions.checkArgument(input != null && !input.isEmpty(), "null timestamp");
          return new DateTime(Long.parseLong(stripQuotes(input)));
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
            return formatter.parseDateTime(stripQuotes(input));
          }
        };

        /*Commenting out until Joda 2.1 is supported
        Pattern pattern = Pattern.compile("[zQ]");
        Matcher matcher = pattern.matcher(format);
        if (matcher.find()) {
          return new Function<String, DateTime>()
          {
            @Override
            public DateTime apply(String input)
            {
              Preconditions.checkArgument(input != null && !input.isEmpty(), "null timestamp");
              return buildTimeStampParser(format, timezones, input).toFormatter().parseDateTime(stripQuotes(input));
            }
          };
        } else {
          final DateTimeFormatter formatter = DateTimeFormat.forPattern(format);
          return new Function<String, DateTime>()
          {
            @Override
            public DateTime apply(String input)
            {
              Preconditions.checkArgument(input != null && !input.isEmpty(), "null timestamp");
              return formatter.parseDateTime(stripQuotes(input));
            }
          };
        }*/
      }
      catch (FormattedException e) {
        Throwables.propagateIfInstanceOf(e, FormattedException.class);
        throw new FormattedException.Builder()
            .withErrorCode(FormattedException.ErrorCode.UNPARSABLE_TIMESTAMP)
            .withMessage(String.format("Unknown timestamp format [%s]", format))
            .build();
      }
      catch (IllegalArgumentException e) {
        Throwables.propagateIfInstanceOf(e, FormattedException.class);
        throw new FormattedException.Builder()
            .withErrorCode(FormattedException.ErrorCode.UNPARSABLE_TIMESTAMP)
            .withMessage(String.format("Unknown timestamp format [%s]", format))
            .build();
      }
    }
  }

  public static String stripQuotes(String input) {
    input = input.trim();
    if(input.charAt(0) == '\"' && input.charAt(input.length()-1) == '\"')
      input = input.substring(1, input.length() - 1).trim();
    return input;
  }

  /*Commenting out until Joda 2.1 is supported
  private static DateTimeFormatterBuilder buildTimeStampParser(
      String format,
      Map<String, DateTimeZone> timezones,
      String input
  )
  {
    DateTimeFormatterBuilder formatBuilder = new DateTimeFormatterBuilder();
    boolean insideLiteral = false;
    int parseablePatternStart = 0;
    for (int i = 0; i < format.length(); i++) {
      char f = format.charAt(i);
      if (f == '\'') {
        insideLiteral = !insideLiteral;
      }
      if (f == 'z' && !insideLiteral) {
        formatBuilder.append(DateTimeFormat.forPattern(format.substring(parseablePatternStart, i)))
                     .appendTimeZoneShortName(timezones);
        parseablePatternStart = i + 1;
      }
      if (f == 'Q' && !insideLiteral) {
        formatBuilder.append(DateTimeFormat.forPattern(format.substring(parseablePatternStart, i)));
        Pattern pattern = Pattern.compile("(GMT[+-]\\d{4})(.)(\\(?[A-Z]{1,5}\\)?)");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
          formatBuilder.appendLiteral(matcher.group(3));
        } else {
          throw new FormattedException.Builder()
              .withErrorCode(FormattedException.ErrorCode.UNPARSABLE_TIMESTAMP)
              .withMessage(
                  String.format(
                      "Timestamp format has primitive Q but input did not contain GMT or UTC offset"
                      + "[%s]", format
                  )
              )
              .build();
        }
        parseablePatternStart = i + 1;
      }
    }
    formatBuilder.append(DateTimeFormat.forPattern(format.substring(parseablePatternStart)));
    return formatBuilder;
  }*/

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
