package com.metamx.common.guava;

/**
 */
public class LimitedAccumulator<OutType, T> implements Accumulator<OutType, T>
{
  private int limit;
  private final Accumulator<OutType, T> accumulator;

  private volatile int count = 0;

  public LimitedAccumulator(
      Accumulator<OutType, T> accumulator, int limit
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
