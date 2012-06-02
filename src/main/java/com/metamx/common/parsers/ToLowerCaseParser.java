package com.metamx.common.parsers;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.metamx.common.exception.FormattedException;

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
  public Map parse(String input) throws FormattedException
  {
    Map<String, Object> line = baseParser.parse(input);
    Map<String, Object> retVal = Maps.newLinkedHashMap();
    for (Map.Entry<String, Object> entry : line.entrySet()) {
      String k = entry.getKey().toLowerCase();

      if(retVal.containsKey(k)) {
        // Duplicate key, case-insensitively
        throw new FormattedException.Builder()
            .withErrorCode(FormattedException.ErrorCode.UNPARSABLE_ROW)
            .withMessage(String.format("Duplicate key [%s]", k))
            .build();
      }

      retVal.put(k, entry.getValue());
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
