package com.metamx.common.parsers;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.metamx.common.collect.Utils;

import java.util.List;
import java.util.Map;

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
    this.fieldNames = Lists.newArrayList(fieldNames);
  }

  public void setFieldNames(String header)
  {
    setFieldNames(splitter.split(header));
  }

  @Override
  public Map<String, Object> parse(String input) throws ParseException
  {
    try {
      Iterable<String> values = splitter.split(input);

      if (fieldNames == null) {
        setFieldNames(ParserUtils.generateFieldNames(Iterators.size(values.iterator())));
      }

      return Utils.zipMapPartial(fieldNames, Iterables.transform(values, valueFunction));
    }
    catch (IllegalArgumentException e) {
      throw new ParseException.Builder()
          .withErrorCode(ParseException.ErrorCode.UNPARSABLE_ROW)
          .withMessage(e.getMessage())
          .build();
    }
  }
}
