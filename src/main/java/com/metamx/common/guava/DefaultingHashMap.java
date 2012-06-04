package com.metamx.common.guava;

import com.google.common.base.Supplier;

import java.util.HashMap;

/**
 */
public class DefaultingHashMap<K, V> extends HashMap<K, V>
{
  private final Supplier<V> supplier;

  public DefaultingHashMap(
      Supplier<V> supplier
  )
  {
    this.supplier = supplier;
  }

  @Override
  public V get(Object o)
  {
    V retVal = super.get(o);

    if (retVal == null) {
      retVal = supplier.get();
      super.put((K) o, retVal);
    }

    return retVal;
  }
}
