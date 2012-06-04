package com.metamx.common.guava;

import com.google.common.base.Function;

/**
 */
public class MappedSequence<T, Out> implements Sequence<Out>
{
  private final Sequence<T> baseSequence;
  private final Function<T, Out> fn;

  public MappedSequence(
      Sequence<T> baseSequence,
      Function<T, Out> fn
  )
  {
    this.baseSequence = baseSequence;
    this.fn = fn;
  }

  @Override
  public <OutType> OutType accumulate(final Accumulator<OutType, Out> accumulator)
  {
    return baseSequence.accumulate(
        new MappingAccumulator<OutType, T, Out>(fn, accumulator)
    );
  }

  @Override
  public <OutType> OutType accumulate(OutType initValue, Accumulator<OutType, Out> accumulator)
  {
    return baseSequence.accumulate(
        initValue,
        new MappingAccumulator<OutType, T, Out>(fn, accumulator)
    );
  }

}
