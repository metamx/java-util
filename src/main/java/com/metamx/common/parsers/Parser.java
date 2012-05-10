package com.metamx.common.parsers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface Parser<K, V>
{
  public Map<K, V> parse(String input) throws IOException;
  public void setFieldNames(Iterable<String> fieldNames);
  public List<String> getFieldNames();
}