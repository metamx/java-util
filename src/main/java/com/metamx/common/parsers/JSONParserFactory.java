package com.metamx.common.parsers;

import com.metamx.common.exception.FormattedException;

import java.util.List;

/**
 */
public class JSONParserFactory implements ParserFactory
{
  @Override
  public Parser makeParser(String delimiter, String header, List<String> columns) throws FormattedException
  {
    return new JSONParser();
  }
}
