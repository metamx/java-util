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

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import java.util.Arrays;
import java.util.List;

/**
 */
public class Sequences
{

  private static final EmptySequence EMPTY_SEQUENCE = new EmptySequence();

  public static <T> Sequence<T> simple(final Iterable<T> iterable)
  {
    return BaseSequence.simple(iterable);
  }

  @SuppressWarnings("unchecked")
  public static <T> Sequence<T> empty()
  {
    return (Sequence<T>) EMPTY_SEQUENCE;
  }

  public static <T> Sequence<T> concat(Iterable<Sequence<T>> sequences)
  {
    return new ConcatSequence<T>(sequences);
  }

  public static <T> Sequence<T> concat(Sequence<T>... sequences)
  {
    return concat(Arrays.asList(sequences));
  }

  public static <From, To> Sequence<To> map(Sequence<From> sequence, Function<From, To> fn)
  {
    return new MappedSequence<From, To>(sequence, fn);
  }

  public static <T> Sequence<T> filter(Sequence<T> sequence, Predicate<T> pred)
  {
    return new FilteredSequence<T>(sequence, pred);
  }

  public static <T, ListType extends List<T>> ListType toList(Sequence<T> seq, ListType list)
  {
    return seq.accumulate(list, Accumulators.<ListType, T>list());
  }

  private static class EmptySequence implements Sequence<Object>
  {
    @Override
    public <OutType> OutType accumulate(OutType initValue, Accumulator<OutType, Object> accumulator)
    {
      return initValue;
    }

    @Override
    public <OutType> Yielder<OutType> toYielder(OutType initValue, YieldingAccumulator<OutType, Object> accumulator)
    {
      return Yielders.done(null);
    }
  }
}
