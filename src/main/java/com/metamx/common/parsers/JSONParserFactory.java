package com.metamx.common.parsers;

import java.io.IOException;
import java.util.List;

/**
 */
public class JSONParserFactory implements ParserFactory
{
  @Override
  public Parser makeParser(String delimiter, String header, List<String> columns) throws IOException
  {
    return new JSONParser();
  }
}
