package com.metamx.common.guava;

import com.google.common.base.Function;

/**
*/
public class MappingAccumulator<OutType, InType, MappedType> implements Accumulator<OutType, InType>
{
  private final Function<InType, MappedType> fn;
  private final Accumulator<OutType, MappedType> accumulator;

  public MappingAccumulator(
      Function<InType, MappedType> fn,
      Accumulator<OutType, MappedType> accumulator
  ) {
    this.fn = fn;
    this.accumulator = accumulator;
  }

  @Override
  public OutType accumulate(OutType accumulated, InType in)
  {
    return accumulator.accumulate(accumulated, fn.apply(in));
  }
}
