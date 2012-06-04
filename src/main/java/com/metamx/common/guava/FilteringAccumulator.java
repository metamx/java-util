package com.metamx.common.guava;

import com.google.common.base.Predicate;

/**
*/
public class FilteringAccumulator<OutType, T> implements Accumulator<OutType, T>
{
  private final Predicate<T> pred;
  private final Accumulator<OutType, T> accumulator;

  public FilteringAccumulator(
      Predicate<T> pred,
      Accumulator<OutType, T> accumulator
  ) {
    this.pred = pred;
    this.accumulator = accumulator;
  }

  @Override
  public OutType accumulate(OutType accumulated, T in)
  {
    if (pred.apply(in)) {
      return accumulator.accumulate(accumulated, in);
    }
    return accumulated;
  }
}
