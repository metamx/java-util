package com.metamx.common.parsers;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
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
      csvParser = new CSVParser(Optional.<String>fromNullable(null), csv);
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
    String csv = "time,value1,value2,value2";
    final Parser<String, Object> csvParser;
    boolean parseable = true;
    try {
      csvParser = new CSVParser(Optional.<String>fromNullable(null), csv);
    }
    catch (Exception e) {
      parseable = false;
    }
    finally {
      Assert.assertFalse(parseable);
    }
  }

  @Test
  public void testCSVParserWithHeader() throws ParseException
  {
    String header = "time,value1,value2";
    final Parser<String, Object> csvParser = new CSVParser(Optional.<String>fromNullable(null), header);
    String body = "hello,world,foo";
    final Map<String, Object> jsonMap = csvParser.parse(body);
    Assert.assertEquals(
        "jsonMap",
        ImmutableMap.of("time", "hello", "value1", "world", "value2", "foo"),
        jsonMap
    );
  }

  @Test
  public void testCSVParserWithoutHeader() throws ParseException
  {
    final Parser<String, Object> csvParser = new CSVParser(Optional.<String>fromNullable(null));
    String body = "hello,world,foo";
    final Map<String, Object> jsonMap = csvParser.parse(body);
    Assert.assertEquals(
        "jsonMap",
        ImmutableMap.of("column_1", "hello", "column_2", "world", "column_3", "foo"),
        jsonMap
    );
  }
}
