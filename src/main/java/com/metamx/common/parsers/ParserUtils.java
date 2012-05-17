package com.metamx.common.parsers;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;

public class ParserUtils
{
  public static final Function<String, String> nullEmptyStringFunction = new Function<String, String>() {
    @Override
    public String apply(String input) {
      if(input == null || input.isEmpty()) return null;
      return input;
    }
  };

  public static ArrayList<String> generateFieldNames(int length) {
    ArrayList<String> names = new ArrayList<String>(length);
    for(int i = 0; i < length; ++i) names.add("column_" + (i + 1));
    return names;
  }

  public static Function<String, DateTime> createTimestampParser(final String format) {
    if(format.equalsIgnoreCase("iso")) {
      return new Function<String, DateTime>() {
        @Override
        public DateTime apply(String input) {
          Preconditions.checkArgument(input != null && !input.isEmpty(), "null timestamp");
          return new DateTime(input);
        }
      };
    } else if(format.equalsIgnoreCase("posix")) {
      return new Function<String, DateTime>() {
        @Override
        public DateTime apply(String input) {
          Preconditions.checkArgument(input != null && !input.isEmpty(), "null timestamp");
          return new DateTime(Long.parseLong(input) * 1000);
        }
      };
    } else {
      try {
        final DateTimeFormatter formatter = DateTimeFormat.forPattern(format);
        return new Function<String, DateTime>() {
          @Override
          public DateTime apply(String input) {
            Preconditions.checkArgument(input != null && !input.isEmpty(), "null timestamp");
            return formatter.parseDateTime(input);
          }
        };
      } catch(IllegalArgumentException e) {
        throw new IllegalArgumentException(String.format("Unknown timestamp format [%s]", format));
      }
    }

  }
}
