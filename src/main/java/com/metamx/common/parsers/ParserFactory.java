package com.metamx.common.parsers;

import com.metamx.common.exception.FormattedException;

import java.util.List;

/**
 */
public interface ParserFactory
{
  public Parser makeParser(String delimiter, String header, List<String> columns) throws FormattedException;
}
