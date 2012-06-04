package com.metamx.common.guava;

import java.util.Iterator;

/**
 */
public class DroppingIterable<T> implements Iterable<T>
{
  private final Iterable<T> delegate;
  private final int numToDrop;

  public DroppingIterable(
      Iterable<T> delegate,
      int numToDrop
  )
  {
    this.delegate = delegate;
    this.numToDrop = numToDrop;
  }

  public Iterator<T> iterator()
  {
    return new DroppingIterator<T>(delegate.iterator(), numToDrop);
  }
}
