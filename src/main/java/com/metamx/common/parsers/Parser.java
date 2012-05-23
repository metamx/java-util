package com.metamx.common.parsers;

import java.util.List;
import java.util.Map;

public interface Parser<K, V>
{
  public Map<K, V> parse(String input) throws ParseException;
  public void setFieldNames(Iterable<String> fieldNames);
  public List<String> getFieldNames();
}