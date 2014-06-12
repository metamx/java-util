package com.metamx.common.guava;

import java.io.Closeable;

/**
 */
public class ResourceClosingSequence<T> extends YieldingSequenceBase<T>
{
  private final Sequence<T> baseSequence;
  private final Closeable closeable;

  public ResourceClosingSequence(Sequence<T> baseSequence, Closeable closeable)
  {
    this.baseSequence = baseSequence;
    this.closeable = closeable;
  }

  @Override
  public <OutType> Yielder<OutType> toYielder(
      OutType initValue, YieldingAccumulator<OutType, T> accumulator
  )
  {
    final Yielder<OutType> baseYielder;
    try {
      baseYielder = baseSequence.toYielder(initValue, accumulator);
    }
    catch (RuntimeException e) {
      CloseQuietly.close(closeable);
      throw e;
    }

    return new ResourceClosingYielder<OutType>(baseYielder, closeable);
  }
}
