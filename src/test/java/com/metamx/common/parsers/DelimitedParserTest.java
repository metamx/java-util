package com.metamx.common.parsers;

import com.google.common.collect.ImmutableMap;
import com.metamx.common.exception.FormattedException;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Map;

public class DelimitedParserTest
{

  @Test
  public void testSimpleHeader()
  {
    String tsv = "time\tvalue1\tvalue2";
    final Parser<String, Object> tsvParser = new DelimitedParser();
    final Map<String, Object> jsonMap = tsvParser.parse(tsv);
    Assert.assertEquals(
        "jsonMap",
        ImmutableMap.of("column_1", "time", "column_2", "value1", "column_3", "value2"),
        jsonMap
    );
  }

  @Test
  public void testInvalidHeader()
  {
    String tsv = "ti/me\tvalu/e1\tval/ue2";
    boolean parseable = true;
    try {
      final Parser<String, Object> tsvParser = new DelimitedParserFactory().makeParser("\t", tsv, null);
    }
    catch (FormattedException e) {
      parseable = false;
    }
    finally {
      Assert.assertFalse(parseable);
    }
  }

}
