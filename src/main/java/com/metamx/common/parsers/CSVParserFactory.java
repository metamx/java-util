package com.metamx.common.parsers;

import java.util.List;

/**
 */
public class CSVParserFactory implements ParserFactory
{
  @Override
  public Parser makeParser(String delimiter, String header, List<String> columns) throws ParseException
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
