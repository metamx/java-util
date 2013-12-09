package com.metamx.common;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.metamx.common.logger.Logger;

import java.util.Random;
import java.util.concurrent.Callable;

public class RetryUtils
{
  public static final Logger log = new Logger(RetryUtils.class);

  public static <T> T retry(final Callable<T> f, Predicate<Throwable> shouldRetry, final int maxTries) throws Exception
  {
    Preconditions.checkArgument(maxTries > 0, "maxTries > 0");
    int nTry = 0;
    while (true) {
      try {
        nTry++;
        return f.call();
      }
      catch (Throwable e) {
        if (nTry < maxTries && shouldRetry.apply(e)) {
          awaitNextRetry(e, nTry);
        } else {
          Throwables.propagateIfInstanceOf(e, Exception.class);
          throw Throwables.propagate(e);
        }
      }
    }
  }

  private static void awaitNextRetry(final Throwable e, final int nTry) throws InterruptedException
  {
    final long baseSleepMillis = 1000;
    final long maxSleepMillis = 60000;
    final double fuzzyMultiplier = Math.min(Math.max(1 + 0.2 * new Random().nextGaussian(), 0), 2);
    final long sleepMillis = (long) (Math.min(maxSleepMillis, baseSleepMillis * Math.pow(2, nTry)) * fuzzyMultiplier);
    log.warn(e, "Failed on try %d, retrying in %,dms.", nTry, sleepMillis);
    Thread.sleep(sleepMillis);
  }
}
