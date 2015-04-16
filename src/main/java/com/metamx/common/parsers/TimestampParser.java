/*
 * Copyright 2011 - 2015 Metamarkets Group Inc.
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
import com.metamx.common.IAE;
import com.metamx.common.logger.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class TimestampParser
{
  private static final Logger log = new Logger(TimestampParser.class);

  public static Function<String, DateTime> createTimestampParser(
      final String format
  )
  {
    if (format.equalsIgnoreCase("auto")) {
      // Could be iso or millis
      return new Function<String, DateTime>()
      {
        @Override
        public DateTime apply(String input)
        {
          Preconditions.checkArgument(input != null && !input.isEmpty(), "null timestamp");

          for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) < '0' || input.charAt(i) > '9') {
              return new DateTime(ParserUtils.stripQuotes(input));
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
          return new DateTime(ParserUtils.stripQuotes(input));
        }
      };
    } else if (format.equalsIgnoreCase("posix")) {
      return new Function<String, DateTime>()
      {
        @Override
        public DateTime apply(String input)
        {
          Preconditions.checkArgument(input != null && !input.isEmpty(), "null timestamp");
          return new DateTime(Long.parseLong(ParserUtils.stripQuotes(input)) * 1000);
        }
      };
    } else if (format.equalsIgnoreCase("ruby")) {
      return new Function<String, DateTime>()
      {
        @Override
        public DateTime apply(String input)
        {
          Preconditions.checkArgument(input != null && !input.isEmpty(), "null timestamp");
          Double ts = Double.parseDouble(ParserUtils.stripQuotes(input));
          Long jts = ts.longValue() * 1000; // ignoring milli secs
          return new DateTime(jts);
        }
      };
    } else if (format.equalsIgnoreCase("millis")) {
      return new Function<String, DateTime>()
      {
        @Override
        public DateTime apply(String input)
        {
          Preconditions.checkArgument(input != null && !input.isEmpty(), "null timestamp");
          return new DateTime(Long.parseLong(ParserUtils.stripQuotes(input)));
        }
      };
    } else if (format.equalsIgnoreCase("nano")) {
      return new Function<String, DateTime>() {
        @Override
        public DateTime apply(String input) {
          Preconditions.checkArgument(input != null && !input.isEmpty(), "null timestamp");
          long timeNs = Long.parseLong(ParserUtils.stripQuotes(input));
          // Convert to milliseconds, effectively: ms = floor(time in ns / 1000000)
          return new DateTime(timeNs / 1000000L);
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
            return formatter.parseDateTime(ParserUtils.stripQuotes(input));
          }
        };
      }
      catch (Exception e) {
        throw new IAE(e, "Unable to parse timestamps with format [%s]", format);
      }
    }
  }
}
