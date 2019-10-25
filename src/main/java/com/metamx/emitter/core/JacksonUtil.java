package com.metamx.emitter.core;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JacksonUtil
{
  public static class CommaDelimitedListDeserializer extends StdScalarDeserializer<List<String>>
  {
    protected CommaDelimitedListDeserializer()
    {
      super(List.class);
    }

    @Override
    public List<String> deserialize(
        JsonParser jsonParser,
        DeserializationContext deserializationContext
    ) throws IOException
    {
      final String text = jsonParser.getText().trim();
      return Arrays.stream(text.split(",")).map(String::trim).collect(Collectors.toList());
    }
  }
}
