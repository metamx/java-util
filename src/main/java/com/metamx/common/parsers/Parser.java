package com.metamx.common.parsers;

import java.io.IOException;
import java.util.Map;

public interface Parser<K, V>
{
  public Map<K, V> parse(String input) throws IOException;
}
