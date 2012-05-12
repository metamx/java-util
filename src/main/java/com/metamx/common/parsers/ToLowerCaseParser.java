package com.metamx.common.parsers;

import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 */
public class ToLowerCaseParser implements Parser<String, Object>
{
  private final Parser baseParser;

  public ToLowerCaseParser(Parser baseParser)
  {
    this.baseParser = baseParser;
  }

  @Override
  public Map parse(String input) throws IOException
  {
    Map<String, Object> line = baseParser.parse(input);
    Map<String, Object> retVal = Maps.newHashMap();
    for (Map.Entry<String, Object> entry : line.entrySet()) {
      retVal.put(entry.getKey().toLowerCase(), entry.getValue());
    }
    return retVal;
  }

  @Override
  public void setFieldNames(Iterable<String> fieldNames)
  {
    baseParser.setFieldNames(fieldNames);
  }

  @Override
  public List<String> getFieldNames()
  {
    return baseParser.getFieldNames();
  }
}
