package com.metamx.common.guava;

import com.google.common.base.Preconditions;

import java.io.IOException;

/**
 * Limits the number of inputs from this sequence.  For example, if there are actually 100 things in the sequence
 * but the limit is set to 10, the Sequence will act as if it only had 10 things.
 */
public class LimitedSequence<T> extends YieldingSequenceBase<T>
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
  public <OutType> Yielder<OutType> toYielder(OutType initValue, YieldingAccumulator<OutType, T> accumulator)
  {
    final LimitedYieldingAccumulator<OutType, T> limitedAccumulator = new LimitedYieldingAccumulator<OutType, T>(accumulator);
    final Yielder<OutType> subYielder = baseSequence.toYielder(initValue, limitedAccumulator);
    return new LimitedYielder<OutType>(subYielder, limitedAccumulator);
  }

  private class LimitedYielder<OutType> implements Yielder<OutType>
  {
    private final Yielder<OutType> subYielder;
    private final LimitedYieldingAccumulator<OutType, T> limitedAccumulator;

    public LimitedYielder(
        Yielder<OutType> subYielder,
        LimitedYieldingAccumulator<OutType, T> limitedAccumulator
    )
    {
      this.subYielder = subYielder;
      this.limitedAccumulator = limitedAccumulator;
    }

    @Override
    public OutType get()
    {
      return subYielder.get();
    }

    @Override
    public Yielder<OutType> next(OutType initValue)
    {
      final Yielder<OutType> next = subYielder.next(initValue);
      if (! limitedAccumulator.withinThreshold()) {
        return Yielders.done(next.get(), next);
      }
      return new LimitedYielder<OutType>(LimitedYielder.this, limitedAccumulator);
    }

    @Override
    public boolean isDone()
    {
      return false;
    }

    @Override
    public void close() throws IOException
    {
      subYielder.close();
    }
  }

  private class LimitedYieldingAccumulator<OutType, T> extends DelegatingYieldingAccumulator<OutType, T>
  {
    int count;

    public LimitedYieldingAccumulator(YieldingAccumulator<OutType, T> accumulator)
    {
      super(accumulator);
      count = 0;
    }

    @Override
    public OutType accumulate(OutType accumulated, T in)
    {
      ++count;

      if (! withinThreshold()) {
        yield();
      }

      return super.accumulate(accumulated, in);
    }

    private boolean withinThreshold() {
      return count < limit;
    }
  }
}
