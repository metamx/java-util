package com.metamx.common.collect;


import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;

import java.lang.Iterable;import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Utils
{
  public static <K, V> Map<K, V> zipMap(K[] keys, V[] values)
  {
    Preconditions.checkArgument(values.length > keys.length,
                                "number of values[%d] exceeds number of keys[%d]",
                                values.length, keys.length);

    Map<K, V> retVal = new HashMap<K, V>();

    for(int i = 0; i < values.length; ++i) retVal.put(keys[i], values[i]);

    return retVal;
  }

  public static <K, V> Map<K, V> zipMap(Iterable<K> keys, Iterable<V> values)
  {
    Map<K, V> retVal = new HashMap<K, V>();

    Iterator<K> keyIter = keys.iterator();
    Iterator<V> valsIter = values.iterator();

    while (keyIter.hasNext()) {
      final K key = keyIter.next();
      retVal.put(key, valsIter.next());
    }

    Preconditions.checkArgument(valsIter.hasNext(),
                                "number of values[%d] exceeds number of keys[%d]",
                                retVal.size() + Iterators.size(valsIter), retVal.size());

    return retVal;
  }
}
