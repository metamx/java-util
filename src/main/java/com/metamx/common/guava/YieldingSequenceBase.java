package com.metamx.common.guava;

import com.google.common.io.Closeables;

import java.io.IOException;

/**
 * A Sequence that is based entirely on the Yielder implementation.
 *
 * This is a base class to simplify the creation of Sequences.
 */
public abstract class YieldingSequenceBase<T> implements Sequence<T>
{
  @Override
  public <OutType> OutType accumulate(OutType initValue, Accumulator<OutType, T> accumulator)
  {
    Yielder<OutType> yielder = toYielder(initValue, YieldingAccumulators.fromAccumulator(accumulator));

    try {
      return yielder.get();
    }
    finally {
      try {
        Closeables.close(yielder, true);
      }
      catch (IOException e) {
        //
      }
    }
  }
}
