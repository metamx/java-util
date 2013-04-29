package com.metamx.common.guava;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

import java.io.IOException;

/**
 */
public class LimitedSequence<T> implements Sequence<T>
{
  private final Sequence<T> baseSequence;
  private final int limit;

  public LimitedSequence(
      Sequence<T> baseSequence,
      int limit
  )
  {
    Preconditions.checkNotNull(baseSequence);
    Preconditions.checkArgument(limit >= 0, "limit is negative");

    this.baseSequence = baseSequence;
    this.limit = limit;
  }

  @Override
  public <OutType> OutType accumulate(OutType initValue, Accumulator<OutType, T> accumulator)
  {
    return baseSequence.accumulate(initValue, new LimitedAccumulator<OutType, T>(limit, accumulator));
  }

  @Override
  public <OutType> Yielder<OutType> toYielder(OutType initValue, YieldingAccumulator<OutType, T> accumulator)
  {
    return baseSequence.toYielder(initValue, accumulator);
  }
}
