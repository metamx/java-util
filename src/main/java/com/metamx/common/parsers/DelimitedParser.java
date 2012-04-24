package com.metamx.common.parsers;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.metamx.common.collect.Utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DelimitedParser implements Parser<String, String>
{
  public static final String DEFAULT_DELIMITER = "\t";

  protected Splitter splitter;
  protected List<String> fieldnames;
  protected String delimiter;

  public DelimitedParser(Iterable<String> fieldnames, String delimiter) {
    init(delimiter);
    setFieldnames(fieldnames);
  }

  public DelimitedParser(String header, String delimiter) {
    init(delimiter);
    setFieldnames(splitter.split(header));
  }

  public DelimitedParser(Iterable<String> fieldnames) {
    this(fieldnames, DEFAULT_DELIMITER);
  }

  public DelimitedParser(String header) {
    this(header, DEFAULT_DELIMITER);
  }

  protected void init(String delimiter) {
    this.delimiter  = delimiter;
    this.splitter   = Splitter.on(delimiter).omitEmptyStrings();
    this.fieldnames = null;
  }

  public void setFieldnames(Iterable<String> fieldnames) {
    fieldnames = Lists.newArrayList(fieldnames);
  }

  @Override
  public Map<String, String> parse(String input) throws IOException {
    Iterable<String> values = splitter.split(input);
    
    try {
      return Utils.zipMap(fieldnames, values);
    }
    catch(IllegalArgumentException e) {
      throw new IOException(e.getMessage());
    }
  }
}
