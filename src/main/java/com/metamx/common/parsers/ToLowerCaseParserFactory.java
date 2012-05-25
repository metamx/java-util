package com.metamx.common.parsers;

import com.metamx.common.exception.FormattedException;

import java.util.List;

/**
 */
public class ToLowerCaseParserFactory implements ParserFactory
{
  private final ParserFactory baseParserFactory;

  public ToLowerCaseParserFactory(ParserFactory baseParserFactory)
  {
    this.baseParserFactory = baseParserFactory;
  }

  @Override
  public Parser makeParser(String delimiter, String header, List<String> columns) throws FormattedException
  {
    return new ToLowerCaseParser(baseParserFactory.makeParser(delimiter, header, columns));
  }
}
