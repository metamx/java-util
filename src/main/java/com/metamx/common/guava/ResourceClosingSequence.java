package com.metamx.common.guava;

import com.google.common.base.Throwables;
import com.google.common.io.Closeables;

import java.io.Closeable;
import java.io.IOException;

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
      try {
        Closeables.close(closeable, true);
      }
      catch (IOException ioe) {
        throw Throwables.propagate(ioe);
      }
      throw e;
    }

    return new ResourceClosingYielder<OutType>(baseYielder, closeable);
  }
}
