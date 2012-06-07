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
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.metamx.common.collect.Utils;
import com.metamx.common.exception.FormattedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CSVParser implements Parser<String, Object>
{
  protected ArrayList<String> fieldNames = null;
  protected au.com.bytecode.opencsv.CSVParser parser = new au.com.bytecode.opencsv.CSVParser();

  public static final String DEFAULT_LIST_DELIMITER = "\u0001";
  protected static final Splitter listSplitter = Splitter.on(DEFAULT_LIST_DELIMITER);


  protected Function<String, Object> valueFunction = new Function<String, Object>()
  {
    @Override
    public Object apply(String input)
    {
      if (input.contains(DEFAULT_LIST_DELIMITER)) {
        return Lists.newArrayList(Iterables.transform(listSplitter.split(input), ParserUtils.nullEmptyStringFunction));
      } else {
        return ParserUtils.nullEmptyStringFunction.apply(input);
      }
    }
  };

  public CSVParser()
  {
  }

  public CSVParser(Iterable<String> fieldNames)
  {
    setFieldNames(fieldNames);
  }

  public CSVParser(String header) throws FormattedException
  {
    setFieldNames(header);
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
          .withErrorCode(FormattedException.ErrorCode.BAD_HEADER)
          .withMessage(String.format("Duplicate entries founds: %s", duplicates.toString()))
          .build();
    }
    this.fieldNames = Lists.newArrayList(fieldNames);
  }

  public void setFieldNames(String header) throws FormattedException
  {
    try {
      setFieldNames(Arrays.asList(parser.parseLine(header)));
    }
    catch (Exception e) {
      throw new FormattedException.Builder()
          .withErrorCode(FormattedException.ErrorCode.BAD_HEADER)
          .withMessage(e.getMessage())
          .build();
    }
  }

  @Override
  public Map<String, Object> parse(String input) throws FormattedException
  {
    try {
      String[] values = parser.parseLine(input);

      if (fieldNames == null) {
        setFieldNames(ParserUtils.generateFieldNames(values.length));
      }

      return Utils.zipMapPartial(fieldNames, Iterables.transform(Lists.newArrayList(values), valueFunction));
    }
    catch (Exception e) {
      throw new FormattedException.Builder()
          .withErrorCode(FormattedException.ErrorCode.UNPARSABLE_ROW)
          .withMessage(e.getMessage())
          .build();
    }
  }
}
