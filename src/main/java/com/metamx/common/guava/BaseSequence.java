package com.metamx.common.guava;

import java.util.Iterator;

/**
 */
public class BaseSequence<T, IterType extends Iterator<T>> implements Sequence<T>
{
  private final IteratorMaker<T, IterType> maker;

  public BaseSequence(
      IteratorMaker<T, IterType> maker
  )
  {
    this.maker = maker;
  }

  @Override
  public <OutType> OutType accumulate(Accumulator<OutType, T> fn)
  {
    return accumulate(null, fn);
  }

  @Override
  public <OutType> OutType accumulate(OutType initValue, Accumulator<OutType, T> fn)
  {
    IterType iter = maker.make();

    OutType retVal = initValue;

    try {
      while (iter.hasNext()) {
        retVal = fn.accumulate(retVal, iter.next());
      }
    }
    finally {
      maker.cleanup(iter);
    }

    return retVal;
  }

  public static interface IteratorMaker<T, IterType extends Iterator<T>>
  {
    public IterType make();
    public void cleanup(IterType iterFromMake);
  }
}
