package com.metamx.common.parsers;

import com.google.common.collect.Lists;
import com.metamx.common.collect.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class CSVParser implements Parser<String, String>
{
  protected ArrayList<String> fieldnames;
  protected au.com.bytecode.opencsv.CSVParser parser = new au.com.bytecode.opencsv.CSVParser();

  public CSVParser(Iterable<String> fieldnames) {
    this.fieldnames = Lists.newArrayList(fieldnames);
  }

  public CSVParser(String header) throws IOException {
    this.fieldnames = Lists.newArrayList(parser.parseLine(header));
  }
  
  @Override
  public Map<String, String> parse(String input) throws IOException
  {
    final String[] values = parser.parseLine(input);

    try {
      return Utils.zipMap(fieldnames.toArray(new String[]{}), values);
    }
    catch(IllegalArgumentException e) {
      throw new IOException(e.getMessage());
    }
  }
}
