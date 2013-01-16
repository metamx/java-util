package com.metamx.common.parsers;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.metamx.common.exception.FormattedException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimestampParser
{

  public static Function<String, DateTime> createTimestampParser(final String format)
  {
    Map<String, DateTimeZone> timezones = new HashMap<String, DateTimeZone>();
    /*Commenting out until Joda 2.1 safely supported
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
    }*/
    return createTimestampParser(format, timezones);
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
        /*Commenting out until Joda 2.1 safely supported
        Pattern pattern = Pattern.compile("[zQ]");
        Matcher matcher = pattern.matcher(format);
        if (matcher.find()) {
          return new Function<String, DateTime>()
          {
            @Override
            public DateTime apply(String input)
            {
              Preconditions.checkArgument(input != null && !input.isEmpty(), "null timestamp");
              return buildTimeStampParser(format, timezones, input).toFormatter().parseDateTime(ParserUtils.stripQuotes(input));
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
              return formatter.parseDateTime(ParserUtils.stripQuotes(input));
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

  /*Commenting out until Joda 2.1 supported
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
        if(i > 0)
          formatBuilder.append(DateTimeFormat.forPattern(format.substring(parseablePatternStart, i)));
        formatBuilder.appendTimeZoneShortName(timezones);
        parseablePatternStart = i + 1;
      }
      if (f == 'Q' && !insideLiteral) {
        formatBuilder.append(DateTimeFormat.forPattern(format.substring(parseablePatternStart, i)));
        Pattern pattern = Pattern.compile("([UTC|GMT][+-]\\d{4})(.)(\\(?[A-Z]{1,5}\\)?)");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
          formatBuilder.appendLiteral(matcher.group(3));
        } else {
          throw new FormattedException.Builder()
              .withErrorCode(FormattedException.ErrorCode.UNPARSABLE_TIMESTAMP)
              .withMessage(
                  String.format(
                      "Timestamp format has primitive Q - time zone nested in parenthesis - but was not preceded by"
                      + "GMT or UTC offset [%s]", format
                  )
              )
              .build();
        }
        parseablePatternStart = i + 1;
      }
    }
    if(parseablePatternStart < format.length())
      formatBuilder.append(DateTimeFormat.forPattern(format.substring(parseablePatternStart)));
    return formatBuilder;
  }*/

}
