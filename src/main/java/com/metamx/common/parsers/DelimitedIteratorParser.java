package com.metamx.common.parsers;

import java.io.IOException;
import java.util.Map;

/**
 */
public class DelimitedIteratorParser implements IteratorParser
{
  private final boolean parseHeader;

  private volatile String seenHeader;

  public DelimitedIteratorParser(
      boolean parseHeader
  )
  {
    this.parseHeader = parseHeader;
  }

  @Override
  public CloseableIterator<Map<String, Object>> parse(final CloseableIterator<String> lines) throws IOException
  {
    final DelimitedParser parser = new DelimitedParser();

    if (parseHeader) {
      String newHeader = lines.next();
      if (seenHeader == null) {
        seenHeader = newHeader;
      } else if (!seenHeader.equals(newHeader)) {
        throw new IllegalArgumentException(String.format("Header mismatch [%s] != [%s]!", seenHeader, newHeader));
      }

      parser.setFieldNames(seenHeader);
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
