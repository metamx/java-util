package com.metamx.common.guava.nary;

/**
 */
public interface TrinaryFn<Type1, Type2, Type3, OutType>
{
  public OutType apply(Type1 arg1, Type2 arg2, Type3 arg3);
}
