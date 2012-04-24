package com.metamx.common.parsers;


import com.google.common.base.Function;

import java.io.IOException;
import java.util.Map;

public class Parsers
{
  public static <K, V> Function<String, Map<K, V>> toFunction(final Parser p) {
    return new Function<String, Map<K, V>>() {
      @Override
      public Map<K, V> apply(String input) {
        try {
          return p.parse(input);
        }
        catch(IOException e) {
          return null;
        }
      }
    };
  }
}
