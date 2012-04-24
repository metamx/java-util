package com.metamx.common.parsers;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Lists;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

public class JSONParser implements Parser<String, String> {
  protected ObjectMapper jsonMapper = new ObjectMapper();

  protected ArrayList<String> fieldnames;

  public JSONParser(Iterable<String> fieldnames) {
    this.fieldnames = Lists.newArrayList(fieldnames);
  }

  @Override
  public Map<String, String> parse(String input) throws IOException
  {
    Map<String, String> map = new HashMap<String, String>();
    JsonNode root = jsonMapper.readTree(input);
    for(String key : fieldnames) {
      JsonNode node = root.path(key);
      // use getValueAsText for compatibility with older jackson implementations on EMR
      map.put(key, (node.isMissingNode() || node.isNull()) ? null : node.getValueAsText());
    }
    return map;
  }

}
