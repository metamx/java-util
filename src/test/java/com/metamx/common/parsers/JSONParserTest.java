package com.metamx.common.parsers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Map;

public class JSONParserTest
{
  private static final String json = "{\"one\": \"foo\", \"two\" : [\"bar\", \"baz\"], \"three\" : \"qux\", \"four\" : null}";
  private static final String numbersJson = "{\"five\" : 5.0, \"six\" : 6, \"many\" : 1234567878900, \"toomany\" : 1234567890000000000000}";
  private static final String whackyCharacterJson = "{\"one\": \"foo\\uD900\"}";

  @Test
  public void testSimple()
  {
    final Parser<String, Object> jsonParser = new JSONParser();
    final Map<String, Object> jsonMap = jsonParser.parse(json);
    Assert.assertEquals(
        "jsonMap",
        ImmutableMap.of("one", "foo", "two", ImmutableList.of("bar", "baz"), "three", "qux"),
        jsonMap
    );
  }

  @Test
  public void testSimpleWithFields()
  {
    final Parser<String, Object> jsonParser = new JSONParser(new ObjectMapper(), Lists.newArrayList("two"));
    final Map<String, Object> jsonMap = jsonParser.parse(json);
    Assert.assertEquals(
        "jsonMap",
        ImmutableMap.of("two", ImmutableList.of("bar", "baz")),
        jsonMap
    );
  }

  @Test
  public void testWithWhackyCharacters()
  {
    final Parser<String, Object> jsonParser = new JSONParser();
    final Map<String, Object> jsonMap = jsonParser.parse(whackyCharacterJson);
    Assert.assertEquals(
        "jsonMap",
        ImmutableMap.of("one", "foo?"),
        jsonMap
    );
  }

  @Test
  public void testWithFields()
  {
    final Parser<String, Object> jsonParser = new JSONParser();
    jsonParser.setFieldNames(ImmutableList.of("two", "three", "five"));
    final Map<String, Object> jsonMap = jsonParser.parse(json);
    Assert.assertEquals(
        "jsonMap",
        ImmutableMap.of("two", ImmutableList.of("bar", "baz"), "three", "qux"),
        jsonMap
    );
  }

  @Test
  public void testWithNumbers()
  {
    final Parser<String, Object> jsonParser = new JSONParser();
    final Map<String, Object> jsonMap = jsonParser.parse(numbersJson);
    Assert.assertEquals(
        "jsonMap",
        ImmutableMap.of("five", 5.0, "six", 6L, "many", 1234567878900L, "toomany", 1.23456789E21),
        jsonMap
    );
  }
}
