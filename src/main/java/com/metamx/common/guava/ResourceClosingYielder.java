package com.metamx.common.guava;

import java.io.Closeable;
import java.io.IOException;

/**
 */
public class ResourceClosingYielder<OutType> implements Yielder<OutType>
{
  private final Yielder<OutType> baseYielder;
  private final Closeable closeable;

  public ResourceClosingYielder(Yielder<OutType> baseYielder, Closeable closeable)
  {
    this.baseYielder = baseYielder;
    this.closeable = closeable;
  }

  @Override
  public OutType get()
  {
    return baseYielder.get();
  }

  @Override
  public Yielder<OutType> next(OutType initValue)
  {
    return new ResourceClosingYielder<OutType>(baseYielder.next(initValue), closeable);
  }

  @Override
  public boolean isDone()
  {
    return baseYielder.isDone();
  }

  @Override
  public void close() throws IOException
  {
    if (closeable != null) {
      closeable.close();
    }
    baseYielder.close();
  }
}
