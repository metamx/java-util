package com.metamx.common.guava.nary;

import java.util.Iterator;

/**
 */
public class BinaryTransformIterable<Type1, Type2, RetType> implements Iterable<RetType>
{
  public static <Type1, Type2, RetType> BinaryTransformIterable<Type1, Type2, RetType> create(
      Iterable<Type1> lhs,
      Iterable<Type2> rhs,
      BinaryFn<Type1, Type2, RetType> fn
  )
  {
    return new BinaryTransformIterable<Type1, Type2, RetType>(lhs, rhs, fn);
  }

  private final Iterable<Type1> lhs;
  private final Iterable<Type2> rhs;
  private final BinaryFn<Type1, Type2, RetType> binaryFn;

  public BinaryTransformIterable(
      Iterable<Type1> lhs,
      Iterable<Type2> rhs,
      BinaryFn<Type1, Type2, RetType> binaryFn
  )
  {
    this.lhs = lhs;
    this.rhs = rhs;
    this.binaryFn = binaryFn;
  }

  @Override
  public Iterator<RetType> iterator()
  {
    return BinaryTransformIterator.create(lhs.iterator(), rhs.iterator(), binaryFn);
  }
}
