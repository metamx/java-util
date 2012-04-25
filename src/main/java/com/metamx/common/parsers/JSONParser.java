package com.metamx.common.parsers;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

public class JSONParser implements Parser<String, Object> {

  protected ObjectMapper jsonMapper = new ObjectMapper();
  protected ArrayList<String> fieldNames = null;

  protected static final Function<JsonNode, String> valueFunction = new Function<JsonNode, String>() {
    @Override
    public String apply(JsonNode node) {
      // use getValueAsText for compatibility with older jackson implementations on EMR
      return (node == null || node.isMissingNode() || node.isNull()) ? null : node.getValueAsText();
    }
  };

  public JSONParser() {
  }

  public JSONParser(Iterable<String> fieldNames) {
    setFieldNames(fieldNames);
  }

  @Override
  public void setFieldNames(Iterable<String> fieldNames) {
    this.fieldNames = Lists.newArrayList(fieldNames);
  }

  @Override
  public Map<String, Object> parse(String input) throws IOException
  {
    Map<String, Object> map = new HashMap<String, Object>();
    JsonNode root = jsonMapper.readTree(input);

    Iterator<String> keysIter = (fieldNames == null ? root.getFieldNames() : fieldNames.iterator());
    
    while(keysIter.hasNext()) {
      String key = keysIter.next();
      JsonNode node = root.path(key);

      if(node.isArray()) {
        map.put(key, Lists.newArrayList(Iterators.transform(node.getElements(), valueFunction)));
      } else {
        map.put(key, valueFunction.apply(node));
      }
    }
    return map;
  }


}
