package com.metamx.common.guava.nary;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 */
public class BinaryTransformIterator<Type1, Type2, RetType> implements Iterator<RetType>
{
  public static <Type1, Type2, RetType> BinaryTransformIterator<Type1, Type2, RetType> create(
      Iterator<Type1> lhs,
      Iterator<Type2> rhs,
      BinaryFn<Type1, Type2, RetType> fn
  )
  {
    return new BinaryTransformIterator<Type1, Type2, RetType>(lhs, rhs, fn);
  }

  private final Iterator<Type1> lhsIter;
  private final Iterator<Type2> rhsIter;
  private final BinaryFn<Type1, Type2, RetType> binaryFn;

  public BinaryTransformIterator(Iterator<Type1> lhsIter, Iterator<Type2> rhsIter, BinaryFn<Type1, Type2, RetType> binaryFn)
  {
    this.lhsIter = lhsIter;
    this.rhsIter = rhsIter;
    this.binaryFn = binaryFn;
  }

  @Override
  public boolean hasNext()
  {
    return lhsIter.hasNext() || rhsIter.hasNext();
  }

  @Override
  public RetType next()
  {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }

    return binaryFn.apply(
        lhsIter.hasNext() ? lhsIter.next() : null,
        rhsIter.hasNext() ? rhsIter.next() : null
    );
  }

  @Override
  public void remove()
  {
    throw new UnsupportedOperationException();
  }
}
