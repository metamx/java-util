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
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.metamx.common.collect.Utils;
import com.metamx.common.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CSVParser implements Parser<String, Object>
{

  private final String listDelimiter;
  private final Splitter listSplitter;
  private final Function<String, Object> valueFunction;

  private final au.com.bytecode.opencsv.CSVParser parser = new au.com.bytecode.opencsv.CSVParser();

  private ArrayList<String> fieldNames = null;
  private final Set<String> skipSplitFields;
  private int[] skipSplitIndices = null;

  public CSVParser(final Optional<String> listDelimiter, String[] skipSplitFields)
  {
    this.listDelimiter = listDelimiter.isPresent() ? listDelimiter.get() : Parsers.DEFAULT_LIST_DELIMITER;
    this.listSplitter = Splitter.on(this.listDelimiter);
    this.valueFunction = new Function<String, Object>()
    {
      @Override
      public Object apply(String input)
      {
        if (input.contains(CSVParser.this.listDelimiter)) {
          return Lists.newArrayList(
              Iterables.transform(
                  listSplitter.split(input),
                  ParserUtils.nullEmptyStringFunction
              )
          );
        } else {
          return ParserUtils.nullEmptyStringFunction.apply(input);
        }
      }
    };
    this.skipSplitFields = skipSplitFields == null
                           ? Collections.<String>emptySet()
                           : new HashSet<>(Arrays.asList(skipSplitFields));
  }

  public CSVParser(final Optional<String> listDelimiter)
  {
    this(listDelimiter, (String[])null);
  }

  public CSVParser(final Optional<String> listDelimiter, final Iterable<String> fieldNames)
  {
    this(listDelimiter, (String[])null);

    setFieldNames(fieldNames);
  }

  public CSVParser(final Optional<String> listDelimiter, final String header)
  {
    this(listDelimiter, (String[])null);

    setFieldNames(header);
  }

  public String getListDelimiter()
  {
    return listDelimiter;
  }

  @Override
  public List<String> getFieldNames()
  {
    return fieldNames;
  }

  @Override
  public void setFieldNames(final Iterable<String> fieldNames)
  {
    ParserUtils.validateFields(fieldNames);
    this.fieldNames = Lists.newArrayList(fieldNames);
    List<Integer> indices = Lists.newArrayListWithCapacity(skipSplitFields.size());
    for (String skipField : skipSplitFields) {
      int index = this.fieldNames.indexOf(skipField);
      if (index >= 0) {
        indices.add(index);
      }
    }
    Collections.sort(indices);
    this.skipSplitIndices = Ints.toArray(indices);
  }

  public void setFieldNames(final String header)
  {
    try {
      setFieldNames(Arrays.asList(parser.parseLine(header)));
    }
    catch (Exception e) {
      throw new ParseException(e, "Unable to parse header [%s]", header);
    }
  }

  @Override
  public Map<String, Object> parse(final String input)
  {
    try {
      String[] values = parser.parseLine(input);

      if (fieldNames == null) {
        setFieldNames(ParserUtils.generateFieldNames(values.length));
      }
      final int[] skipper = skipSplitIndices;
      if (skipper == null || skipper.length == 0) {
        return Utils.zipMapPartial(fieldNames, Iterables.transform(Lists.newArrayList(values), valueFunction));
      }
      List<Object> objects = Lists.newArrayListWithCapacity(values.length);
      for (int i = 0, j = 0; i < values.length; i++) {
        if (j < skipper.length && i == skipper[j++]) {
          objects.add(ParserUtils.nullEmptyStringFunction.apply(values[i]));
        } else {
          objects.add(valueFunction.apply(values[i]));
        }
      }
      return Utils.zipMapPartial(fieldNames, objects);
    }
    catch (Exception e) {
      throw new ParseException(e, "Unable to parse row [%s]", input);
    }
  }
}
