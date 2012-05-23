package com.metamx.common.parsers;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.metamx.common.collect.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

  public CSVParser(String header) throws ParseException
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
    this.fieldNames = Lists.newArrayList(fieldNames);
  }

  public void setFieldNames(String header) throws ParseException
  {
    try {
      setFieldNames(Arrays.asList(parser.parseLine(header)));
    }
    catch (Exception e) {
      throw new ParseException.Builder()
          .withErrorCode(ParseException.ErrorCode.BAD_HEADER)
          .withMessage(e.getMessage())
          .build();
    }
  }

  @Override
  public Map<String, Object> parse(String input) throws ParseException
  {
    try {
      String[] values = parser.parseLine(input);

      if (fieldNames == null) {
        setFieldNames(ParserUtils.generateFieldNames(values.length));
      }

      return Utils.zipMapPartial(fieldNames, Iterables.transform(Lists.newArrayList(values), valueFunction));
    }
    catch (Exception e) {
      throw new ParseException.Builder()
          .withErrorCode(ParseException.ErrorCode.UNPARSABLE_ROW)
          .withMessage(e.getMessage())
          .build();
    }
  }
}
