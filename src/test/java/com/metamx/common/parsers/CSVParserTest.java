package com.metamx.common.parsers;

import com.google.common.collect.ImmutableMap;
import com.metamx.common.exception.FormattedException;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Map;

public class CSVParserTest
{

  @Test
  public void testSimpleHeader()
  {
    String csv = "time,value1,value2";
    final Parser<String, Object> csvParser = new CSVParser();
    final Map<String, Object> jsonMap = csvParser.parse(csv);
    Assert.assertEquals(
        "jsonMap",
        ImmutableMap.of("column_1", "time", "column_2", "value1", "column_3", "value2"),
        jsonMap
    );
  }

  @Test
  public void testInvalidHeader()
  {
    String csv = "ti/me,valu/e1,val/ue2";
    boolean parseable = true;
    try {
      final Parser<String, Object> csvParser = new CSVParserFactory().makeParser(null, csv, null);
    }
    catch (FormattedException e) {
      parseable = false;
    }
    finally {
      Assert.assertFalse(parseable);
    }
  }

}
