package com.metamx.common.guava;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 */
public class LimitedSequenceTest
{
  @Test
  public void testSanityAccumulate() throws Exception
  {
    final List<Integer> nums = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    final int threshold = 5;
    SequenceTestHelper.testAll(
        Sequences.limit(Sequences.simple(nums), threshold),
        Lists.newArrayList(Iterables.limit(nums, threshold))
    );
  }

  @Test
  public void testTwo() throws Exception
  {
    final List<Integer> nums = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    final int threshold = 2;

    SequenceTestHelper.testAll(
        Sequences.limit(Sequences.simple(nums), threshold),
        Lists.newArrayList(Iterables.limit(nums, threshold))
    );
  }

  @Test
  public void testOne() throws Exception
  {
    final List<Integer> nums = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    final int threshold = 1;

    SequenceTestHelper.testAll(
        Sequences.limit(Sequences.simple(nums), threshold),
        Lists.newArrayList(Iterables.limit(nums, threshold))
    );
  }

  @Test
  public void testNoSideEffects() throws Exception
  {
    final List<Integer> nums = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    final AtomicLong accumulated = new AtomicLong(0);
    final Sequence<Integer> seq = Sequences.limit(
        Sequences.simple(
            Iterables.transform(
                nums,
                new Function<Integer, Integer>()
                {
                  @Override
                  public Integer apply(@Nullable Integer input)
                  {
                    accumulated.addAndGet(input);
                    return input;
                  }
                }
            )
        ),
        5
    );

    Assert.assertEquals(10, seq.accumulate(0, new IntAdditionAccumulator()).intValue());
    Assert.assertEquals(10, accumulated.get());
    Assert.assertEquals(10, seq.accumulate(0, new IntAdditionAccumulator()).intValue());
    Assert.assertEquals(20, accumulated.get());
  }

  private static class IntAdditionAccumulator implements Accumulator<Integer, Integer>
  {
    @Override
    public Integer accumulate(Integer accumulated, Integer in)
    {
      return accumulated + in;
    }
  }
}
