package com.metamx.common.guava;

import com.google.common.base.Function;

import java.util.Map;

/**
 */
public class Fns
{
  public static Function<String, String[]> splitFn(final String splitChar, final int numCols)
  {
    return new Function<String, String[]>()
    {
      public String[] apply(String input)
      {
        return input.split(splitChar, numCols);
      }
    };
  }

  public static <KeyType, OutType> Function<Map<KeyType, OutType>, OutType> getFromMap(final KeyType key)
  {
    return new Function<Map<KeyType, OutType>, OutType>()
    {
      @Override
      public OutType apply(Map<KeyType, OutType> in)
      {
        return in.get(key);
      }
    };
  }
}
