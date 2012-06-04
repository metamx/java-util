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

package com.metamx.common;

import com.google.common.base.Function;

/**
 */
public class Pair<T1, T2>
{
  
  public static <T1, T2> Pair<T1, T2> of(T1 lhs, T2 rhs) {
    return new Pair<T1, T2>(lhs, rhs);
  }
  
  public final T1 lhs;
  public final T2 rhs;

  public Pair(T1 lhs, T2 rhs)
  {
    this.lhs = lhs;
    this.rhs = rhs;
  }

  @Override
  public String toString()
  {
    return "Pair{" +
           "lhs=" + lhs +
           ", rhs=" + rhs +
           '}';
  }

  public static <T1, T2> Function<Pair<T1, T2>, T1> lhsFn()
  {
    return new Function<Pair<T1, T2>, T1>()
    {
      @Override
      public T1 apply(Pair<T1, T2> input)
      {
        return input.lhs;
      }
    };
  }

  public static <T1, T2> Function<Pair<T1, T2>, T2> rhsFn()
  {
    return new Function<Pair<T1, T2>, T2>()
    {
      @Override
      public T2 apply(Pair<T1, T2> input)
      {
        return input.rhs;
      }
    };
  }
}
