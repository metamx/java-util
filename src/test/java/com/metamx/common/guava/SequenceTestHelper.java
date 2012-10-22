/*
 * Copyright 2011,2012 Metamarkets Group Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.metamx.common.guava;

import junit.framework.Assert;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 */
public class SequenceTestHelper
{
  public static void testAll(Sequence<Integer> seq, List<Integer> nums) throws IOException
  {
    testAll("", seq, nums);
  }

  public static void testAll(String prefix, Sequence<Integer> seq, List<Integer> nums) throws IOException
  {
    testAccumulation(prefix, seq, nums);
    testYield(prefix, seq, nums);
  }

  public static void testYield(final String prefix, Sequence<Integer> seq, final List<Integer> nums) throws IOException
  {
    testYield(prefix, 3, seq, nums);
  }

  public static void testYield(
      final String prefix,
      final int numToTake,
      Sequence<Integer> seq,
      final List<Integer> nums
  ) throws IOException
  {
    Iterator<Integer> numsIter = nums.iterator();
    Yielder<Integer> yielder = seq.toYielder(
        0, new YieldingAccumulator<Integer, Integer>()
    {
      final Iterator<Integer> valsIter = nums.iterator();
      int count = 0;

      @Override
      public Integer accumulate(Integer accumulated, Integer in)
      {
        if (++count >= numToTake) {
          count = 0;
          yield();
        }

        Assert.assertEquals(prefix, valsIter.next(), in);
        return accumulated + in;
      }
    }
    );

    while (numsIter.hasNext()) {
      int expectedSum = 0;
      for (int i = 0; i < numToTake && numsIter.hasNext(); ++i) {
        expectedSum += numsIter.next();
      }

      Assert.assertFalse(prefix, yielder.isDone());
      Assert.assertEquals(prefix, expectedSum, yielder.get().intValue());

      yielder = yielder.next(0);
    }

    Assert.assertTrue(prefix, yielder.isDone());
    yielder.close();
  }


  public static void testAccumulation(final String prefix, Sequence<Integer> seq, final List<Integer> nums)
  {
    int expectedSum = 0;
    for (Integer num : nums) {
      expectedSum += num;
    }

    int sum = seq.accumulate(
        0, new Accumulator<Integer, Integer>()
    {
      Iterator<Integer> valsIter = nums.iterator();

      @Override
      public Integer accumulate(Integer accumulated, Integer in)
      {
        Assert.assertEquals(prefix, valsIter.next(), in);
        return accumulated + in;
      }
    }
    );

    Assert.assertEquals(prefix, expectedSum, sum);
  }
}
