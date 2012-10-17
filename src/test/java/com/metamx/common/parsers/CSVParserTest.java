package com.metamx.common.parsers;

import com.google.common.collect.ImmutableMap;
import com.metamx.common.exception.FormattedException;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Map;

public class CSVParserTest
{

  @Test
  public void testValidHeader()
  {
    String csv = "time,value1,value2";
    final Parser<String, Object> csvParser;
    boolean parseable = true;
    try {
      csvParser = new CSVParserFactory().makeParser(null, csv, null);
    } catch(FormattedException e) {
      parseable = false;
    } finally {
      Assert.assertTrue(parseable);
    }
  }

  @Test
  public void testInvalidHeader()
  {
    String csv = "time,value1,value2,value2";
    final Parser<String, Object> csvParser;
    boolean parseable = true;
    try {
      csvParser = new CSVParserFactory().makeParser(null, csv, null);
    } catch(FormattedException e) {
      parseable = false;
    } finally {
      Assert.assertFalse(parseable);
    }
  }

  @Test
  public void testCSVParserWithHeader()
  {
    String header = "time,value1,value2";
    final Parser<String, Object> csvParser = new CSVParserFactory().makeParser(null, header, null);
    String body = "hello,world,foo";
    final Map<String, Object> jsonMap = csvParser.parse(body);
    Assert.assertEquals(
        "jsonMap",
        ImmutableMap.of("time", "hello", "value1", "world", "value2", "foo"),
        jsonMap
    );
  }

  @Test
  public void testCSVParserWithoutHeader()
  {
    final Parser<String, Object> csvParser = new CSVParserFactory().makeParser(null, null, null);
    String body = "hello,world,foo";
    final Map<String, Object> jsonMap = csvParser.parse(body);
    Assert.assertEquals(
        "jsonMap",
        ImmutableMap.of("column_1", "hello", "column_2", "world", "column_3", "foo"),
        jsonMap
    );
  }


}
