package com.metamx.common.concurrent;

import com.google.common.base.Function;

import java.util.concurrent.ThreadFactory;

/**
 */
public class FunctionalThreadFactory implements ThreadFactory
{
  private final ThreadFactory delegate;

  public FunctionalThreadFactory(final String name)
  {
    this(
        new ThreadFactory()
        {
          @Override
          public Thread newThread(Runnable runnable)
          {
            return new Thread(runnable, name);
          }
        }
    );
  }

  public FunctionalThreadFactory(ThreadFactory delegate)
  {
    this.delegate = delegate;
  }

  @Override
  public Thread newThread(Runnable runnable)
  {
    return delegate.newThread(runnable);
  }

  public FunctionalThreadFactory transform(Function<ThreadFactory, ThreadFactory> fn)
  {
    return new FunctionalThreadFactory(fn.apply(delegate));
  }

  public FunctionalThreadFactory transformThread(final Function<Thread, Thread> fn)
  {
    return new FunctionalThreadFactory(new ThreadFactory()
    {
      @Override
      public Thread newThread(Runnable runnable)
      {
        return fn.apply(delegate.newThread(runnable));
      }
    });
  }

  public FunctionalThreadFactory daemonize()
  {
    return transformThread(
        new Function<Thread, Thread>()
        {
          @Override
          public Thread apply(Thread input)
          {
            input.setDaemon(true);
            return input;
          }
        }
    );
  }
}
