package com.metamx.common.parsers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 */
public class IteratorParser
{
  private final boolean parseHeader;
  private final String delimiter;
  private final List<String> columns;
  private final ParserFactory parserFactory;

  private volatile String seenHeader = null;

  public IteratorParser(
      boolean parseHeader,
      String delimiter,
      List<String> columns,
      ParserFactory parserFactory
  )
  {
    this.parseHeader = parseHeader;
    this.delimiter = delimiter;
    this.columns = columns;
    this.parserFactory = parserFactory;
  }

  public CloseableIterator<Map<String, Object>> parse(final CloseableIterator<String> lines) throws IOException
  {
    if (parseHeader) {
      String newHeader = lines.next();
      if (seenHeader == null) {
        seenHeader = newHeader;
      } else if (!seenHeader.equals(newHeader)) {
        throw new IllegalArgumentException(String.format("Header mismatch [%s] != [%s]!", seenHeader, newHeader));
      }
    }

    final Parser parser = parserFactory.makeParser(delimiter, seenHeader, columns);

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