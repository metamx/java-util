package com.metamx.common.collect;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class MoreIterators
{
  public static <X> Iterator<X> bracket(final Iterator<X> iterator, final Runnable before, final Runnable after)
  {
    return before(after(iterator, after), before);
  }

  /**
   * Run f immediately before the first element of iterator is generated.
   * Exceptions raised by f will prevent the requested behavior on the
   * underlying iterator, and can be handled by the caller.
   */
  public static <X> Iterator<X> before(final Iterator<X> iterator, final Runnable f)
  {
    return new Iterator<X>()
    {
      private Runnable fOnlyOnce = new RunOnlyOnce(f);

      @Override
      public boolean hasNext()
      {
        fOnlyOnce.run();
        return iterator.hasNext();
      }

      @Override
      public X next()
      {
        fOnlyOnce.run();
        return iterator.next();
      }

      @Override
      public void remove()
      {
        fOnlyOnce.run();
        iterator.remove();
      }
    };
  }

  /**
   * Run f immediately after the last element of iterator is generated.
   * Exceptions must not be raised by f.
   */
  public static <X> Iterator<X> after(final Iterator<X> iterator, final Runnable f)
  {
    return new Iterator<X>()
    {
      private Runnable fOnlyOnce = new RunOnlyOnce(f);

      @Override
      public boolean hasNext()
      {
        final boolean hasNext = iterator.hasNext();
        if (!hasNext) {
          fOnlyOnce.run();
        }
        return hasNext;
      }

      @Override
      public X next()
      {
        try {
          return iterator.next();
        }
        catch (NoSuchElementException e) {
          fOnlyOnce.run(); // (f exceptions are prohibited because they destroy e here)
          throw e;
        }
      }

      @Override
      public void remove()
      {
        iterator.remove();
      }
    };
  }

  private static class RunOnlyOnce implements Runnable
  {
    private final Runnable f;

    private volatile boolean hasRun = false;

    public RunOnlyOnce(Runnable f)
    {
      this.f = f;
    }

    @Override
    public void run()
    {
      if (!hasRun) {
        f.run();
        hasRun = true;
      }
    }
  }
}