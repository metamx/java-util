/*
 * Copyright 2011,2012 Metamarkets Group Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.metamx.common.guava;

import com.google.common.base.Throwables;
import com.metamx.common.ISE;

import java.io.IOException;
import java.util.Iterator;

/**
 */
public class ConcatSequence<T> implements Sequence<T>
{
  private final Iterable<? extends Sequence<T>> baseSequences;

  public ConcatSequence(
      Iterable<? extends Sequence<T>> baseSequences
  )
  {
    this.baseSequences = baseSequences;
  }

  @Override
  public <OutType> OutType accumulate(OutType initValue, Accumulator<OutType, T> accumulator)
  {
    OutType retVal = initValue;
    for (Sequence<T> baseSequence : baseSequences) {
      retVal = baseSequence.accumulate(retVal, accumulator);
    }
    return retVal;
  }

  @Override
  public <OutType> Yielder<OutType> toYielder(final OutType initValue, final YieldingAccumulator<OutType, T> accumulator)
  {
    return fromSequenceIterator(baseSequences.iterator(), initValue, accumulator);
  }

  private <OutType> Yielder<OutType> fromSequenceIterator(
      final Iterator<? extends Sequence<T>> iter, final OutType initValue, final YieldingAccumulator<OutType, T> accumulator
  )
  {
    if (!iter.hasNext()) {
      return Yielders.done(null);
    }

    Yielder<OutType> initialYielder = iter.next().toYielder(initValue, accumulator);
    while (initialYielder.isDone() && iter.hasNext()) {
      try {
        initialYielder.close();
      }
      catch (IOException e) {
        throw Throwables.propagate(e);
      }

      initialYielder = iter.next().toYielder(initValue, accumulator);
    }

    if (accumulator.yielded()) {
      return makeYieldedYielder(iter, initialYielder, accumulator);
    }

    return makeNextYielder(iter, initialYielder, initValue, accumulator);
  }

  private <OutType> Yielder<OutType> makeNextYielder(
      final Iterator<? extends Sequence<T>> iter, final Yielder<OutType> yielder, OutType initValue, final YieldingAccumulator<OutType, T> accumulator
  )
  {
    Yielder<OutType> nextYielder = yielder.next(initValue);
    while (!accumulator.yielded() && iter.hasNext()) {
      try {
        if (nextYielder.isDone()) {
          while (nextYielder.isDone() && iter.hasNext()) {
            nextYielder.close();
            nextYielder = iter.next().toYielder(initValue, accumulator);
          }
        }
        else {
          OutType nextInitValue = nextYielder.get();
          nextYielder.close();

          initValue = nextInitValue;
          nextYielder = iter.next().toYielder(nextInitValue, accumulator);
        }
      }
      catch (IOException e) {
        throw Throwables.propagate(e);
      }
    }

    if (accumulator.yielded()) {
      return makeYieldedYielder(iter, nextYielder, accumulator);
    }

    if (!iter.hasNext()) {
      return nextYielder;
    }

    throw new ISE("WTF? I don't believe this can happen.");
  }

  private <OutType> Yielder<OutType> makeYieldedYielder(
      final Iterator<? extends Sequence<T>> iter,
      final Yielder<OutType> yielder,
      final YieldingAccumulator<OutType, T> accumulator
  )
  {
    return new Yielder<OutType>()
    {
      @Override
      public OutType get()
      {
        return yielder.get();
      }

      @Override
      public Yielder<OutType> next(OutType initValue)
      {
        return makeNextYielder(iter, yielder, initValue, accumulator);
      }

      @Override
      public boolean isDone()
      {
        return false;
      }

      @Override
      public void close() throws IOException
      {
        yielder.close();
      }
    };
  }
}
