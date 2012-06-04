package com.metamx.common.collect;

import java.util.HashMap;

// Can't find a good way to abstract over which aggregator representation is used,
// so I just pick Double/MutableDouble.
public class AggregatingMap<K> extends HashMap<K, Double>
{
  public void add(K k, double n)
  {
    final Double value = get(k);

    if (value == null) {
      put(k, n);
      return;
    }

    put(k, value + n);
  }
}
