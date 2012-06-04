package com.metamx.common.guava;

import java.util.Iterator;

/**
 */
public class DroppingIterator<T> implements Iterator<T>
{
  private final Iterator<T> delegate;
  private final int numToDrop;
  private boolean dropped = false;

  public DroppingIterator(
      Iterator<T> delegate,
      int numToDrop
  )
  {
    this.delegate = delegate;
    this.numToDrop = numToDrop;
  }

  public boolean hasNext()
  {
    if (! dropped) {
      for (int i = 0; i < numToDrop; ++i) {
        delegate.next();
      }
      dropped = true;
    }

    return delegate.hasNext();
  }

  public T next()
  {
    if (! dropped) {
      for (int i = 0; i < numToDrop; ++i) {
        delegate.next();
      }
      dropped = true;
    }
    return delegate.next();
  }

  public void remove()
  {
    throw new UnsupportedOperationException();
  }
}
