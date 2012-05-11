package com.metamx.common.parsers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 */
public class CSVIteratorParser implements IteratorParser
{
  private final boolean parseHeader;
  private final List<String> columns;

  private volatile String seenHeader;

  public CSVIteratorParser(
      boolean parseHeader,
      List<String> columns
  )
  {
    this.parseHeader = parseHeader;
    this.columns = columns;
  }

  @Override
  public CloseableIterator<Map<String, Object>> parse(final CloseableIterator<String> lines) throws IOException
  {
    final CSVParser parser;
    if (parseHeader) {
      String newHeader = lines.next();
      if (seenHeader == null) {
        seenHeader = newHeader;
      } else if (!seenHeader.equals(newHeader)) {
        throw new IllegalArgumentException(String.format("Header mismatch [%s] != [%s]!", seenHeader, newHeader));
      }
    }

    if (columns != null) {
      parser = new CSVParser(columns);
    } else if (parseHeader) {
      parser = new CSVParser(seenHeader);
    } else {
      parser = new CSVParser();
    }

    return new CloseableIterator<Map<String, Object>>()
    {
      @Override
      public void close() throws IOException
      {
        lines.close();
      }

      @Override
      public boolean hasNext()
      {
        return lines.hasNext();
      }

      @Override
      public Map<String, Object> next()
      {
        try {
          return parser.parse(lines.next());
        }
        catch (Exception e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public void remove()
      {
        throw new UnsupportedOperationException();
      }
    };
  }
}