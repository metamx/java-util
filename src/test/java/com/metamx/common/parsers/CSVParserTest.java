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
  public void testHeaderWithDuplicate()
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

  @Test
  public void testUnbalancedQuotes()
  {
    String csv = "hello,\"world";
    final Parser<String, Object> csvParser;
    boolean parseable = true;
    try {
      csvParser = new CSVParserFactory().makeParser(",", csv, null);
    } catch(FormattedException e) {
      parseable = false;
    } finally {
      Assert.assertFalse(parseable);
    }
  }

  @Test
  public void testEscapedHeader()
  {
    String csv = "\"\"excel, with comma\"\",\"with \"\"quote\"\"\",\"\"with \backslash";
    final Parser<String, Object> csvParser;
    boolean parseable = true;
    try {
      csvParser = new CSVParserFactory().makeParser(",", csv, null);
    } catch(FormattedException e) {
      parseable = false;
    } finally {
      Assert.assertTrue(parseable);
    }
  }

  @Test
  public void testParserWithEscapes()
  {
    String header = "\"excel, with comma\",\"with \"\"quote\"\"\",\"\"with \backslash";
    final Parser<String, Object> csvParser;
    boolean parseable = true;
    try {
      csvParser = new CSVParserFactory().makeParser(",", header, null);

      String body = "hel\"\"lo,world,foo";
      final Map<String, Object> jsonMap = csvParser.parse(body);
      Assert.assertEquals(
          "jsonMap",
          ImmutableMap.of("excel, with comma", "hel\"lo", "with \"quote\"", "world", "\"with \backslash", "foo"),
          jsonMap
      );
    } catch(FormattedException e) {
      parseable = false;
    } finally {
      Assert.assertTrue(parseable);
    }
  }

  @Test
  public void testParserWithDoubleQuotes()
  {
    String header = "hello \"\" world,how are you";
    final Parser<String, Object> csvParser;
    boolean parseable = true;
    try {
      csvParser = new CSVParserFactory().makeParser(",", header, null);

      String body = "hello,world";
      final Map<String, Object> jsonMap = csvParser.parse(body);
      Assert.assertEquals(
          "jsonMap",
          ImmutableMap.of("hello \" world", "hello", "how are you", "world"),
          jsonMap
      );
    } catch(FormattedException e) {
      parseable = false;
    } finally {
      Assert.assertTrue(parseable);
    }
  }

  @Test
  public void testParserWithSlashQuotes()
  {
    String header = "hello \\\" world,how are you";
    final Parser<String, Object> csvParser;
    boolean parseable = true;
    try {
      csvParser = new CSVParserFactory().makeParser(",", header, null);

      String body = "hello,world";
      final Map<String, Object> jsonMap = csvParser.parse(body);
      Assert.assertEquals(
          "jsonMap",
          ImmutableMap.of("hello \" world", "hello", "how are you", "world"),
          jsonMap
      );
    } catch(FormattedException e) {
      parseable = false;
    } finally {
      Assert.assertTrue(parseable);
    }
  }

  @Test
  public void testParserWithSlashQuotes2()
  {
    String header = "hello \\\"\" world,how are you";
    final Parser<String, Object> csvParser;
    boolean parseable = true;
    try {
      csvParser = new CSVParserFactory().makeParser(",", header, null);
    } catch(FormattedException e) {
      parseable = false;
    } finally {
      Assert.assertFalse(parseable);
    }
  }

}
