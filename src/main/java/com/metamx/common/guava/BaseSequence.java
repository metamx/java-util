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

import com.google.common.io.Closeables;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 */
public class BaseSequence<T, IterType extends Iterator<T>> implements Sequence<T>
{
  private final IteratorMaker<T, IterType> maker;

  public static <T> Sequence<T> simple(final Iterable<T> iterable)
  {
    return new BaseSequence<T, Iterator<T>>(
        new BaseSequence.IteratorMaker<T, Iterator<T>>()
        {
          @Override
          public Iterator<T> make()
          {
            return iterable.iterator();
          }

          @Override
          public void cleanup(Iterator<T> iterFromMake)
          {

          }
        }
    );
  }

  public BaseSequence(
      IteratorMaker<T, IterType> maker
  )
  {
    this.maker = maker;
  }

  @Override
  public <OutType> OutType accumulate(OutType initValue, final Accumulator<OutType, T> fn)
  {
    Yielder<OutType> yielder = null;
    try {
      yielder = toYielder(initValue, YieldingAccumulators.fromAccumulator(fn));      return yielder.isDone() ? initValue : yielder.get();
    }
    finally {
      Closeables.closeQuietly(yielder);
    }
  }

  @Override
  public <OutType> Yielder<OutType> toYielder(OutType initValue, YieldingAccumulator<OutType, T> accumulator)
  {
    return makeYielder(initValue, accumulator, maker.make());
  }

  private <OutType> Yielder<OutType> makeYielder(
      OutType initValue,
      final YieldingAccumulator<OutType, T> accumulator,
      final IterType iter
  )
  {
    if (! iter.hasNext()) {
      return Yielders.done(
          new Closeable()
          {
            @Override
            public void close() throws IOException
            {
              maker.cleanup(iter);
            }
          }
      );
    }

    OutType retVal = initValue;
    while (!accumulator.yielded() && iter.hasNext()) {
      retVal = accumulator.accumulate(retVal, iter.next());
    }

    final OutType finalRetVal = retVal;
    return new Yielder<OutType>()
    {
      @Override
      public OutType get()
      {
        return finalRetVal;
      }

      @Override
      public Yielder<OutType> next(OutType initValue)
      {
        accumulator.reset();
        return makeYielder(initValue, accumulator, iter);
      }

      @Override
      public boolean isDone()
      {
        return false;
      }

      @Override
      public void close() throws IOException
      {
        maker.cleanup(iter);
      }
    };
  }

  public static interface IteratorMaker<T, IterType extends Iterator<T>>
  {
    public IterType make();
    public void cleanup(IterType iterFromMake);
  }
}
