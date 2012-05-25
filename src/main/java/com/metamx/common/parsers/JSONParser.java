package com.metamx.common.parsers;


import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.metamx.common.exception.FormattedException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JSONParser implements Parser<String, Object>
{

  protected ObjectMapper jsonMapper = new ObjectMapper();
  protected ArrayList<String> fieldNames = null;

  protected static final Function<JsonNode, String> valueFunction = new Function<JsonNode, String>()
  {
    @Override
    public String apply(JsonNode node)
    {
      // use getValueAsText for compatibility with older jackson implementations on EMR
      return (node == null || node.isMissingNode() || node.isNull()) ? null : node.getValueAsText();
    }
  };

  public JSONParser()
  {
  }

  public JSONParser(Iterable<String> fieldNames)
  {
    setFieldNames(fieldNames);
  }

  @Override
  public List<String> getFieldNames()
  {
    return fieldNames;
  }

  @Override
  public void setFieldNames(Iterable<String> fieldNames)
  {
    this.fieldNames = Lists.newArrayList(fieldNames);
  }

  @Override
  public Map<String, Object> parse(String input) throws FormattedException
  {
    try {
      Map<String, Object> map = new LinkedHashMap<String, Object>();
      JsonNode root = jsonMapper.readTree(input);

      Iterator<String> keysIter = (fieldNames == null ? root.getFieldNames() : fieldNames.iterator());

      while (keysIter.hasNext()) {
        String key = keysIter.next();
        JsonNode node = root.path(key);

        if (node.isArray()) {
          map.put(key, Lists.newArrayList(Iterators.transform(node.getElements(), valueFunction)));
        } else {
          map.put(key, valueFunction.apply(node));
        }
      }
      return map;
    }
    catch (Exception e) {
      throw new FormattedException.Builder()
          .withErrorCode(FormattedException.ErrorCode.UNPARSABLE_ROW)
          .withMessage(e.getMessage())
          .build();
    }
  }
}