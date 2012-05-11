package com.metamx.common.parsers;

import java.io.IOException;
import java.util.Map;

/**
 */
public class JSONIteratorParser implements IteratorParser
{
  public JSONIteratorParser()
  {
  }

  @Override
  public CloseableIterator<Map<String, Object>> parse(final CloseableIterator<String> lines) throws IOException
  {
    final JSONParser parser = new JSONParser();

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