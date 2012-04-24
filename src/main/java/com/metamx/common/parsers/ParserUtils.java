package com.metamx.common.parsers;

import java.util.ArrayList;

public class ParserUtils
{
  public static ArrayList<String> generateFieldNames(int length) {
    ArrayList<String> names = new ArrayList<String>();
    for(int i = 0; i < length; ++i) names.add("column_" + (i + 1));
    return names;
  }
}
