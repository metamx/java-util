package com.metamx.common.parsers;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

public class TimestampParserTest
{

  @Test
  public void testTimestampParser() throws Exception {
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

}
