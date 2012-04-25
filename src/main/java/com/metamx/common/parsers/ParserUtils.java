package com.metamx.common.parsers;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.ArrayList;

public class ParserUtils
{
  public static final Function<String, String> nullEmptyStringFunction = new Function<String, String>() {
    @Override
    public String apply(String input) {
      if(input == null || input.length() == 0) return null;
      return input;
    }
  };

  public static ArrayList<String> generateFieldNames(int length) {
    ArrayList<String> names = new ArrayList<String>(length);
    for(int i = 0; i < length; ++i) names.add("column_" + (i + 1));
    return names;
  }
}
