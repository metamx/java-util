package com.metamx.common.guava;

import org.junit.Assert;
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 */
public class ResourceClosingSequenceTest
{
  @Test
  public void testSanity() throws Exception
  {
    final AtomicInteger closedCounter = new AtomicInteger(0);
    Closeable closeable = new Closeable()
    {
      @Override
      public void close() throws IOException
      {
        closedCounter.incrementAndGet();
      }
    };

    final List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5);

    SequenceTestHelper.testAll(Sequences.withBaggage(Sequences.simple(nums), closeable), nums);

    Assert.assertEquals(3, closedCounter.get());

    closedCounter.set(0);
    SequenceTestHelper.testClosed(closedCounter, Sequences.withBaggage(new UnsupportedSequence(), closeable));
  }
}
