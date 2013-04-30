package com.metamx.common.guava;

/**
 */
public class LimitedYieldingAccumulator<OutType, T> extends YieldingAccumulator<OutType, T>
{
  private int limit;
  private final YieldingAccumulator<OutType, T> delegate;

  private volatile int count = 0;

  public LimitedYieldingAccumulator(
      YieldingAccumulator<OutType, T> delegate, int limit
  )
  {
    this.limit = limit;
    this.delegate = delegate;
  }

  @Override
  public void yield()
  {
    delegate.yield();
  }

  @Override
  public boolean yielded()
  {
    return delegate.yielded();
  }

  @Override
  public void reset()
  {
    delegate.reset();
  }

  @Override
  public OutType accumulate(OutType accumulated, T in)
  {
    if (count < limit) {
      count++;
      return delegate.accumulate(accumulated, in);
    }
    return accumulated;
  }
}
