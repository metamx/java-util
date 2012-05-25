package com.metamx.common.parsers;

import com.metamx.common.exception.FormattedException;

import java.util.List;

/**
 */
public class CSVParserFactory implements ParserFactory
{
  @Override
  public Parser makeParser(String delimiter, String header, List<String> columns) throws FormattedException
  {
    final CSVParser parser = new CSVParser();

    if (columns != null) {
      parser.setFieldNames(columns);
    } else if (header != null) {
      parser.setFieldNames(header);
    }

    return parser;
  }
}
