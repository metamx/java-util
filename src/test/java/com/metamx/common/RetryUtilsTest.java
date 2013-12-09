package com.metamx.common;

import com.google.common.base.Predicate;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class RetryUtilsTest
{
  private static final Predicate<Throwable> isTransient = new Predicate<Throwable>()
  {
    @Override
    public boolean apply(Throwable e)
    {
      return e instanceof IOException && e.getMessage().equals("what");
    }
  };

  @Test
  public void testImmediateSuccess() throws Exception
  {
    final AtomicInteger count = new AtomicInteger();
    final String result = RetryUtils.retry(
        new Callable<String>()
        {
          @Override
          public String call() throws Exception
          {
            count.incrementAndGet();
            return "hey";
          }
        },
        isTransient,
        2
    );
    Assert.assertEquals("result", "hey", result);
    Assert.assertEquals("count", 1, count.get());
  }

  @Test
  public void testEventualFailure() throws Exception
  {
    final AtomicInteger count = new AtomicInteger();
    boolean threwExpectedException = false;
    try {
      RetryUtils.retry(
          new Callable<String>()
          {
            @Override
            public String call() throws Exception
            {
              count.incrementAndGet();
              throw new IOException("what");
            }
          },
          isTransient,
          2
      );
    }
    catch (IOException e) {
      threwExpectedException = e.getMessage().equals("what");
    }
    Assert.assertTrue("threw expected exception", threwExpectedException);
    Assert.assertEquals("count", 2, count.get());
  }

  @Test
  public void testEventualSuccess() throws Exception
  {
    final AtomicInteger count = new AtomicInteger();
    final String result = RetryUtils.retry(
        new Callable<String>()
        {
          @Override
          public String call() throws Exception
          {
            if (count.incrementAndGet() >= 2) {
              return "hey";
            } else {
              throw new IOException("what");
            }
          }
        },
        isTransient,
        3
    );
    Assert.assertEquals("result", "hey", result);
    Assert.assertEquals("count", 2, count.get());
  }

  @Test
  public void testExceptionPredicateNotMatching() throws Exception
  {
    final AtomicInteger count = new AtomicInteger();
    boolean threwExpectedException = false;
    try {
      RetryUtils.retry(
          new Callable<String>()
          {
            @Override
            public String call() throws Exception
            {
              if (count.incrementAndGet() >= 2) {
                return "hey";
              } else {
                throw new IOException("uhh");
              }
            }
          },
          isTransient,
          3
      );
    }
    catch (IOException e) {
      threwExpectedException = e.getMessage().equals("uhh");
    }
    Assert.assertTrue("threw expected exception", threwExpectedException);
    Assert.assertEquals("count", 1, count.get());
  }

}
