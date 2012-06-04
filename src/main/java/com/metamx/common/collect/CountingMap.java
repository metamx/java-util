package com.metamx.common.collect;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

// Can't find a good way to abstract over which counter representation is used,
// so I just pick Long/MutableLong.
public class CountingMap<K> extends AbstractMap<K, Long>
{
  private final HashMap<K, AtomicLong> counts = new HashMap<K, AtomicLong>();

  public void add(K k, Long n)
  {
    if (!counts.containsKey(k)) {
      counts.put(k, new AtomicLong(0));
    }
    counts.get(k).addAndGet(n);
  }

  public Set<Entry<K, Long>> entrySet()
  {
    return Maps.transformValues(
        counts,
        new Function<AtomicLong, Long>()
        {
          @Override
          public Long apply(AtomicLong n)
          {
            return n.get();
          }
        }
    ).entrySet();
  }
}
