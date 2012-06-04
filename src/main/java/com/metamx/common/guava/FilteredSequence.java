package com.metamx.common.guava;

import com.google.common.base.Predicate;

/**
 */
public class FilteredSequence<T> implements Sequence<T>
{
  private final Sequence<T> baseSequence;
  private final Predicate<T> pred;

  public FilteredSequence(
      Sequence<T> baseSequence,
      Predicate<T> pred
  )
  {
    this.baseSequence = baseSequence;
    this.pred = pred;
  }

  @Override
  public <OutType> OutType accumulate(final Accumulator<OutType, T> accumulator)
  {
    return baseSequence.accumulate(
        new FilteringAccumulator<OutType, T>(pred, accumulator)
    );
  }

  @Override
  public <OutType> OutType accumulate(OutType initValue, Accumulator<OutType, T> accumulator)
  {
    return baseSequence.accumulate(
        initValue,
        new FilteringAccumulator<OutType, T>(pred, accumulator)
    );
  }

}
