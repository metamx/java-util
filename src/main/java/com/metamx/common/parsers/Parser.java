package com.metamx.common.parsers;

import com.metamx.common.exception.FormattedException;

import java.util.List;
import java.util.Map;

public interface Parser<K, V>
{
  public Map<K, V> parse(String input) throws FormattedException;
  public void setFieldNames(Iterable<String> fieldNames);
  public List<String> getFieldNames();
}