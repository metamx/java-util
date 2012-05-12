package com.metamx.common.parsers;

import java.io.IOException;
import java.util.List;

/**
 */
public class DelimitedParserFactory implements ParserFactory
{
  @Override
  public Parser makeParser(String delimiter, String header, List<String> columns) throws IOException
  {
    final DelimitedParser parser = new DelimitedParser(delimiter);
    
    if (columns != null) {
      parser.setFieldNames(columns);
    } else if (header != null) {
      parser.setFieldNames(header);
    }

    return parser;
  }
}
