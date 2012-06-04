package com.metamx.common.guava;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 */
public class IteratorWithBaggage<T> implements Iterator<T>, Closeable
{
  private final Iterator<T> baseIter;
  private final Closeable baggage;

  public IteratorWithBaggage(
      Iterator<T> baseIter,
      Closeable baggage
  )
  {
    this.baseIter = baseIter;
    this.baggage = baggage;
  }

  @Override
  public boolean hasNext()
  {
    return baseIter.hasNext();
  }

  @Override
  public T next()
  {
    return baseIter.next();
  }

  @Override
  public void remove()
  {
    baseIter.remove();
  }

  @Override
  public void close() throws IOException
  {
    baggage.close();
  }
}
