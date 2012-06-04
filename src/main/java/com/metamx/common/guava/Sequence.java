package com.metamx.common.guava;

/**
 */
public interface Sequence<T>
{
  public <OutType> OutType accumulate(Accumulator<OutType, T> accumulator);
  public <OutType> OutType accumulate(OutType initValue, Accumulator<OutType, T> accumulator);
}
