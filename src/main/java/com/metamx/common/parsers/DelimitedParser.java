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

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.metamx.common.collect.Utils;
import com.metamx.common.exception.FormattedException;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DelimitedParser implements Parser<String, Object>
{
  public static final String DEFAULT_DELIMITER = "\t";
  public static final String DEFAULT_LIST_DELIMITER = "\u0001";

  protected String delimiter;
  protected String listDelimiter;

  protected Splitter splitter;
  protected Splitter listSplitter;

  protected List<String> fieldNames;

  protected Function<String, Object> valueFunction = new Function<String, Object>()
  {
    @Override
    public Object apply(String input)
    {
      if (input.contains(listDelimiter)) {
        return Lists.newArrayList(Iterables.transform(listSplitter.split(input), ParserUtils.nullEmptyStringFunction));
      } else {
        return ParserUtils.nullEmptyStringFunction.apply(input);
      }
    }
  };

  public DelimitedParser(String delimiter, String listDelimiter)
  {
    init(delimiter, listDelimiter);
  }

  public DelimitedParser(String delimiter)
  {
    this(delimiter, DEFAULT_LIST_DELIMITER);
  }

  public DelimitedParser()
  {
    this(DEFAULT_DELIMITER, DEFAULT_LIST_DELIMITER);
  }

  public DelimitedParser(Iterable<String> fieldNames, String delimiter, String listDelimiter)
  {
    this(delimiter, listDelimiter);
    setFieldNames(fieldNames);
  }

  public DelimitedParser(Iterable<String> fieldNames, String delimiter)
  {
    this(fieldNames, delimiter, DEFAULT_LIST_DELIMITER);
  }

  public DelimitedParser(Iterable<String> fieldNames)
  {
    this(fieldNames, DEFAULT_DELIMITER, DEFAULT_LIST_DELIMITER);
  }

  protected void init(String delimiter, String listDelimiter)
  {
    this.delimiter = delimiter;
    this.listDelimiter = listDelimiter;
    this.splitter = Splitter.on(delimiter);
    this.listSplitter = Splitter.on(listDelimiter);
    this.fieldNames = null;
  }

  @Override
  public List<String> getFieldNames()
  {
    return fieldNames;
  }

  @Override
  public void setFieldNames(Iterable<String> fieldNames)
  {
    Set<String> duplicates = ParserUtils.findDuplicates(fieldNames);
    if (!duplicates.isEmpty()) {
      throw new FormattedException.Builder()
          .withErrorCode(FormattedException.ErrorCode.UNPARSABLE_HEADER)
          .withDetails(
              ImmutableMap.<String, Object>of(
                  "subErrorCode", FormattedException.UnparsableHeaderSubErrorCode.DUPLICATE_KEY,
                  "duplicates", duplicates
              )
          )
          .withMessage(String.format("Duplicate entries founds: %s", duplicates.toString()))
          .build();
    }
    this.fieldNames = Lists.newArrayList(fieldNames);
  }

  public void setFieldNames(String header)
  {
    setFieldNames(splitter.split(header));
  }

  @Override
  public Map<String, Object> parse(String input) throws FormattedException
  {
    try {
      Iterable<String> values = splitter.split(input);

      if (fieldNames == null) {
        setFieldNames(ParserUtils.generateFieldNames(Iterators.size(values.iterator())));
      }

      return Utils.zipMapPartial(fieldNames, Iterables.transform(values, valueFunction));
    }
    catch (IllegalArgumentException e) {
      throw new FormattedException.Builder()
          .withErrorCode(FormattedException.ErrorCode.UNPARSABLE_ROW)
          .withMessage(e.getMessage())
          .build();
    }
  }
}
