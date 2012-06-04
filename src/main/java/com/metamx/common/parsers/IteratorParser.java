/*
 * Copyright 2011,2012 Metamarkets Group Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.metamx.common.parsers;

import com.metamx.common.exception.FormattedException;

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

  public CloseableIterator<Map<String, Object>> parse(final CloseableIterator<String> lines) throws FormattedException
  {
    if (parseHeader) {
      String newHeader = lines.next();
      if (seenHeader == null) {
        seenHeader = newHeader;
      } else if (!seenHeader.equals(newHeader)) {
        throw new FormattedException.Builder()
            .withErrorCode(FormattedException.ErrorCode.BAD_HEADER)
            .withMessage(String.format("Header mismatch [%s] != [%s]!", seenHeader, newHeader))
            .build();
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
          Map<String, Object> parsed = parser.parse(lines.next());

          //TODO: this validation needs to go elsewhere
          for (String key : parsed.keySet()) {
            if (key.contains("/")) {
                        throw new FormattedException.Builder()
              .withErrorCode(FormattedException.ErrorCode.UNPARSABLE_ROW)
              .withMessage("Columns cannot contain '/'")
              .build();
            }
          }

          return parsed;
        }
        catch (FormattedException e) {
          throw new FormattedException.Builder()
              .withErrorCode(e.getErrorCode())
              .withDetails(e.getDetails())
              .withMessage(e.getMessage())
              .build();
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