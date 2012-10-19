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

/**
 */
public class ConcatSequence<T> implements Sequence<T>
{
  private final Iterable<Sequence<T>> baseSequences;

  public ConcatSequence(
      Iterable<Sequence<T>> baseSequences
  )
  {
    this.baseSequences = baseSequences;
  }

  @Override
  public <OutType> OutType accumulate(Accumulator<OutType, T> accumulator)
  {
    return accumulate(null, accumulator);
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
}
