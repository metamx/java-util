package com.metamx.common.guava;

import com.google.common.base.Predicate;

/**
 */
public class LimitedAccumulator<OutType, T> implements Accumulator<OutType, T>
{
  private int limit;
  private final Accumulator<OutType, T> accumulator;

  private volatile int count = 0;

  public LimitedAccumulator(
      int limit,
      Accumulator<OutType, T> accumulator
  )
  {
    this.limit = limit;
    this.accumulator = accumulator;
  }

  @Override
  public OutType accumulate(OutType accumulated, T in)
  {
    if (count < limit) {
      count++;
      return accumulator.accumulate(accumulated, in);
    }
    return accumulated;
  }
}
