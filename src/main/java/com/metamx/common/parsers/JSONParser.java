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


import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.metamx.common.exception.FormattedException;
import com.metamx.common.exception.SubErrorHolder;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JSONParser implements Parser<String, Object>
{

  private final ObjectMapper jsonMapper = new ObjectMapper();
  private ArrayList<String> fieldNames = null;

  private static final Function<JsonNode, String> valueFunction = new Function<JsonNode, String>()
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
    ParserUtils.validateFields(fieldNames);
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
          final List<String> nodeValue = Lists.newArrayListWithExpectedSize(node.size());
          for(final JsonNode subnode : node) {
            final String subnodeValue = valueFunction.apply(subnode);
            if(subnodeValue != null) {
              nodeValue.add(subnodeValue);
            }
          }
          map.put(key, nodeValue);
        } else {
          final String nodeValue = valueFunction.apply(node);
          if(nodeValue != null) {
            map.put(key, nodeValue);
          }
        }
      }
      return map;
    }
    catch (Exception e) {
      Throwables.propagateIfPossible(e, FormattedException.class);
      throw new FormattedException.Builder()
          .withErrorCode(FormattedException.ErrorCode.UNPARSABLE_ROW)
          .withMessage(e.getMessage())
          .build();
    }
  }
}