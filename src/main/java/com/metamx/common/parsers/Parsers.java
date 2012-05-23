package com.metamx.common.parsers;


import com.google.common.base.Function;

import java.util.Map;

public class Parsers
{
  public static <K, V> Function<String, Map<K, V>> toFunction(final Parser p) {

    /**
     * Creates a Function object wrapping the given parser.
     * Parser inputs that throw an ParseException are mapped to null.
     */
    return new Function<String, Map<K, V>>() {
      @Override
      public Map<K, V> apply(String input) {
        try {
          return p.parse(input);
        }
        catch(ParseException e) {
          return null;
        }
      }
    };
  }
}
