package com.metamx.common.guava.nary;

import com.google.common.collect.Lists;
import com.metamx.common.guava.Comparators;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 */
public class SortedMergeIteratorTest
{
  @Test
  public void testSanity() throws Exception
  {
    SortedMergeIterator<Integer, Integer> iter = SortedMergeIterator.create(
        Arrays.asList(1, 4, 5, 7, 9).iterator(),
        Arrays.asList(1, 2, 3, 6, 7, 8, 9, 10, 11).iterator(),
        Comparators.<Integer>comparable(),
        new BinaryFn<Integer, Integer, Integer>()
        {
          @Override
          public Integer apply(Integer arg1, Integer arg2)
          {
            return arg1 == null ? arg2 : arg2 == null ? arg1 : arg1 + arg2;
          }
        }
    );

    Assert.assertEquals(
        Arrays.asList(2, 2, 3, 4, 5, 6, 14, 8, 18, 10, 11),
        Lists.newArrayList(iter)
    );
  }
}
