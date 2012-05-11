package com.metamx.common.parsers;

import java.io.IOException;
import java.util.Map;

/**
 */
public interface IteratorParser
{
  public CloseableIterator<Map<String, Object>> parse(CloseableIterator<String> lines) throws IOException;
}
