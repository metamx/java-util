package com.metamx.common.guava;

/**
 */
public interface Accumulator<AccumulatedType, InType>
{
  public AccumulatedType accumulate(AccumulatedType accumulated, InType in);
}
