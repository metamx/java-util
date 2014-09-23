/*
 * Copyright 2011,2012 Metamarkets Group Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.metamx.common.parsers;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.metamx.common.logger.Logger;

import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JSONParser implements Parser<String, Object>
{
  private static final Logger log = new Logger(JSONParser.class);
  private static final ObjectMapper jsonMapper = new ObjectMapper();
  private static final Function<JsonNode, String> valueFunction = new Function<JsonNode, String>()
  {
    @Override
    public String apply(JsonNode node)
    {
      final String s = (node == null || node.isMissingNode() || node.isNull()) ? null : node.asText();
      final CharsetEncoder enc = Charsets.UTF_8.newEncoder();
      if (s != null && !enc.canEncode(s)) {
        // Some whacky characters are in this string (e.g. \uD900). These are problematic because they are decodeable
        // by new String(...) but will not encode into the same character. This dance here will replace these
        // characters with something more sane.
        return new String(s.getBytes(Charsets.UTF_8), Charsets.UTF_8);
      } else {
        return s;
      }
    }
  };

  private ArrayList<String> fieldNames = null;

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
    ParserUtils.validateFields(fieldNames);
    this.fieldNames = Lists.newArrayList(fieldNames);
  }

  @Override
  public Map<String, Object> parse(String input)
  {
    try {
      Map<String, Object> map = new LinkedHashMap<String, Object>();
      JsonNode root = jsonMapper.readTree(input);

      Iterator<String> keysIter = (fieldNames == null ? root.fieldNames() : fieldNames.iterator());

      while (keysIter.hasNext()) {
        String key = keysIter.next();
        JsonNode node = root.path(key);

        if (node.isArray()) {
          final List<String> nodeValue = Lists.newArrayListWithExpectedSize(node.size());
          for (final JsonNode subnode : node) {
            final String subnodeValue = valueFunction.apply(subnode);
            if (subnodeValue != null) {
              nodeValue.add(subnodeValue);
            }
          }
          map.put(key, nodeValue);
        } else {
          final String nodeValue = valueFunction.apply(node);
          if (nodeValue != null) {
            map.put(key, nodeValue);
          }
        }
      }
      return map;
    }
    catch (Exception e) {
      throw new ParseException(e, "Unable to parse row [%s]", input);
    }
  }
}