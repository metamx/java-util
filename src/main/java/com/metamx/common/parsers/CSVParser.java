package com.metamx.common.parsers;

import com.google.common.collect.Lists;
import com.metamx.common.collect.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class CSVParser implements Parser<String, String> {
  protected ArrayList<String> fieldnames = null;
  protected au.com.bytecode.opencsv.CSVParser parser = new au.com.bytecode.opencsv.CSVParser();

  public CSVParser() {
  }

  public CSVParser(Iterable<String> fieldnames) {
    setFieldnames(fieldnames);
  }

  public CSVParser(String header) throws IOException {
    setFieldnames(Arrays.asList(parser.parseLine(header)));
  }

  public void setFieldnames(Iterable<String> fieldnames) {
    this.fieldnames = Lists.newArrayList(fieldnames);
  }

  @Override
  public Map<String, String> parse(String input) throws IOException
  {
    String[] values = parser.parseLine(input);
    for(int i = 0; i < values.length; ++i) if(values[i].equals("")) values[i] = null;

    if(fieldnames == null) {
      setFieldnames(ParserUtils.generateFieldNames(values.length));
    }

    try {
      return Utils.zipMapPartial(fieldnames.toArray(new String[]{}), values);
    }
    catch(IllegalArgumentException e) {
      throw new IOException(e.getMessage());
    }
  }
}
