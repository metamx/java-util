package com.metamx.common.parsers;

import com.google.common.collect.ImmutableMap;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Map;

public class DelimitedParserTest
{

  @Test
  public void testValidHeader()
  {
    String tsv = "time\tvalue1\tvalue2";
    final Parser<String, Object> delimitedParser;
    boolean parseable = true;
    try {
      delimitedParser = new DelimitedParserFactory().makeParser("\t", null, tsv, null);
    }
    catch (Exception e) {
      parseable = false;
    }
    finally {
      Assert.assertTrue(parseable);
    }
  }

  @Test
  public void testInvalidHeader()
  {
    String tsv = "time\tvalue1\tvalue2\tvalue2";
    final Parser<String, Object> delimitedParser;
    boolean parseable = true;
    try {
      delimitedParser = new DelimitedParserFactory().makeParser("\t", null, tsv, null);
    }
    catch (Exception e) {
      parseable = false;
    }
    finally {
      Assert.assertFalse(parseable);
    }
  }

  @Test
  public void testTSVParserWithHeader()
  {
    String header = "time\tvalue1\tvalue2";
    final Parser<String, Object> delimitedParser = new DelimitedParserFactory().makeParser("\t", null, header, null);
    String body = "hello\tworld\tfoo";
    final Map<String, Object> jsonMap = delimitedParser.parse(body);
    Assert.assertEquals(
        "jsonMap",
        ImmutableMap.of("time", "hello", "value1", "world", "value2", "foo"),
        jsonMap
    );
  }

  @Test
  public void testTSVParserWithoutHeader()
  {
    final Parser<String, Object> delimitedParser = new DelimitedParserFactory().makeParser("\t", null, null, null);
    String body = "hello\tworld\tfoo";
    final Map<String, Object> jsonMap = delimitedParser.parse(body);
    Assert.assertEquals(
        "jsonMap",
        ImmutableMap.of("column_1", "hello", "column_2", "world", "column_3", "foo"),
        jsonMap
    );
  }
}
