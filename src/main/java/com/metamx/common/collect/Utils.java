package com.metamx.common.collect;


import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;

import java.lang.Iterable;import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Utils
{
  public static <K, V> Map<K, V> zipMap(K[] keys, V[] values) {
    return zipMap(keys, values, false);
  }
  
  public static <K, V> Map<K, V> zipMap(K[] keys, V[] values, boolean allowUnusedKeys)
  {
    Preconditions.checkArgument(values.length <= keys.length,
                                "number of values[%s] exceeds number of keys[%s]",
                                values.length, keys.length);

    Preconditions.checkArgument(allowUnusedKeys || values.length == keys.length,
                                "number of values[%s] less than number of keys[%s]",
                                values.length, keys.length);


    Map<K, V> retVal = new HashMap<K, V>();

    for(int i = 0; i < values.length; ++i) retVal.put(keys[i], values[i]);

    return retVal;
  }

  public static <K, V> Map<K, V> zipMap(Iterable<K> keys, Iterable<V> values) {
    return zipMap(keys, values, false);
  }
  
  public static <K, V> Map<K, V> zipMap(Iterable<K> keys, Iterable<V> values, boolean allowUnusedKeys)
  {
    Map<K, V> retVal = new HashMap<K, V>();

    Iterator<K> keysIter = keys.iterator();
    Iterator<V> valsIter = values.iterator();

    while (keysIter.hasNext()) {
      final K key = keysIter.next();

      Preconditions.checkArgument(valsIter.hasNext() || allowUnusedKeys,
                                  "number of values[%s] less than number of keys",
                                  retVal.size());
      
      if(valsIter.hasNext()) retVal.put(key, valsIter.next());
    }

    Preconditions.checkArgument(!valsIter.hasNext(),
                                "number of values[%s] exceeds number of keys[%s]",
                                retVal.size() + Iterators.size(valsIter), retVal.size());

    return retVal;
  }
}
