package com.metamx.common.guava;

/**
*/
public class UnsupportedSequence implements Sequence<Integer>
{
  @Override
  public <OutType> OutType accumulate(
      OutType initValue, Accumulator<OutType, Integer> accumulator
  )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public <OutType> Yielder<OutType> toYielder(
      OutType initValue, YieldingAccumulator<OutType, Integer> accumulator
  )
  {
    throw new UnsupportedOperationException();
  }
}
