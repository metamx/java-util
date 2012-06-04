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
