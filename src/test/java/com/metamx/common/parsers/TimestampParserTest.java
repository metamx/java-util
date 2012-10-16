package com.metamx.common.parsers;

import com.google.common.base.Function;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

public class TimestampParserTest
{

  @Test
  public void testStripQuotes() throws Exception {
    Assert.assertEquals("hello world", TimestampParser.stripQuotes("\"hello world\""));
    Assert.assertEquals("hello world", TimestampParser.stripQuotes("    \"    hello world   \"    "));
  }

  @Test
  public void testAuto() throws Exception {
    final Function<String, DateTime> parser = ParserUtils.createTimestampParser("auto");
    Assert.assertEquals(new DateTime("2009-02-13T23:31:30Z"), parser.apply("1234567890000"));
    Assert.assertEquals(new DateTime("2009-02-13T23:31:30Z"), parser.apply("2009-02-13T23:31:30Z"));
  }

  @Test
  public void testTimeStampParserWithQuotes() throws Exception {
    DateTime d = new DateTime(1994, 11, 9, 4, 0, DateTimeZone.forOffsetHours(-8));
    Assert.assertEquals(d,
                        ParserUtils.createTimestampParser("EEE MMM dd HH:mm:ss z yyyy")
                                   .apply(" \" Wed Nov 9 04:00:00 PST 1994 \"  ")
    );
  }

  @Test
  public void testTimeStampParserWithShortTimeZone() throws Exception {
    DateTime d = new DateTime(1994, 11, 9, 4, 0, DateTimeZone.forOffsetHours(-8));
    Assert.assertEquals(d,
                        ParserUtils.createTimestampParser("EEE MMM dd HH:mm:ss z yyyy")
                                   .apply("Wed Nov 9 04:00:00 PST 1994")
    );
    Assert.assertEquals(d,
                        ParserUtils.createTimestampParser("EEE MMM dd HH:mm:ss z yyyy 'hello'")
                                   .apply("Wed Nov 9 04:00:00 PST 1994 hello")
    );
    Assert.assertEquals(d,
                        ParserUtils.createTimestampParser("EEE MMM dd HH:mm:ss 'hello' z yyyy")
                                   .apply("Wed Nov 9 04:00:00 hello PST 1994")
    );
    Assert.assertEquals(d,
                        ParserUtils.createTimestampParser("EEE MMM dd HH:mm:ss 'helloz' z yyyy 'hello'")
                                   .apply("Wed Nov 9 04:00:00 helloz PST 1994 hello")
    );
  }

  @Test
  public void testTimeStampParserWithLongTimeZone() throws Exception {

    long millis1 = new DateTime(1994, 11, 9, 4, 0, DateTimeZone.forOffsetHours(-8)).getMillis();
    long millis2 = new DateTime(1994, 11, 9, 4, 0, DateTimeZone.forOffsetHours(-6)).getMillis();
    Assert.assertEquals(millis1,
                        ParserUtils.createTimestampParser("EEE MMM dd HH:mm:ss 'GMT'Z Q yyyy")
                                   .apply("Wed Nov 9 04:00:00 GMT-0800 PST 1994")
                                   .getMillis()
    );
    Assert.assertEquals(millis2,
                        ParserUtils.createTimestampParser("EEE MMM dd HH:mm:ss 'GMT'Z Q yyyy 'helloQ'")
                                   .apply("Wed Nov 9 04:00:00 GMT-0600 CST 1994 helloQ")
                                   .getMillis()
    );
    Function<String, DateTime> parser = ParserUtils.createTimestampParser("EEE MMM dd HH:mm:ss 'GMT'Z Q yyyy 'helloQ'");
    Assert.assertEquals(millis1, parser.apply("Wed Nov 9 04:00:00 GMT-0800 PST 1994 helloQ").getMillis());
    Assert.assertEquals(millis2, parser.apply("Wed Nov 9 04:00:00 GMT-0600 CST 1994 helloQ").getMillis());

  }


}
