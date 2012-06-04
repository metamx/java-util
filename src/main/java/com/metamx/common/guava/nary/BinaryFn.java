package com.metamx.common.guava.nary;

/**
 */
public interface BinaryFn<Type1, Type2, OutType>
{
  public OutType apply(Type1 arg1, Type2 arg2);
}
