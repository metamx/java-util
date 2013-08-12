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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.metamx.common.parsers.CloseableIterator;
import junit.framework.Assert;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 */
public class ConcatSequenceTest
{
  @Test
  public void testAccumulationSingle() throws Exception
  {
    testAll(
        Arrays.asList(
            Arrays.asList(1, 2, 3, 4, 5)
        )
    );
  }

  @Test
  public void testAccumulationMultiple() throws Exception
  {
    testAll(
        Arrays.asList(
            Arrays.asList(1, 2, 3, 4, 5),
            Arrays.asList(6, 7, 8),
            Arrays.asList(9, 10, 11, 12)
        )
    );
  }

  @Test
  public void testAccumulationMultipleAndEmpty() throws Exception
  {
    testAll(
        Arrays.asList(
            Arrays.asList(1, 2, 3, 4, 5),
            Arrays.<Integer>asList(),
            Arrays.asList(6, 7, 8),
            Arrays.asList(9, 10, 11, 12)
        )
    );
  }

  @Test
  public void testAccumulationMultipleAndEmpty1() throws Exception
  {
    testAll(
        Arrays.asList(
            Arrays.asList(1, 2, 3, 4, 5),
            Arrays.<Integer>asList(),
            Arrays.asList(6, 7, 8),
            Arrays.asList(9, 10, 11, 12),
            Arrays.<Integer>asList()
        )
    );
  }

  @Test
  public void testAccumulationMultipleAndEmpty2() throws Exception
  {
    testAll(
        Arrays.asList(
            Arrays.<Integer>asList(),
            Arrays.asList(1, 2, 3, 4, 5),
            Arrays.<Integer>asList(),
            Arrays.asList(6, 7, 8),
            Arrays.asList(9, 10, 11, 12)
        )
    );
  }

  @Test
  public void testClosingOfSequenceSequence() throws Exception
  {
    final int[] closedCount = {0};
    final Sequence<Integer> seq = Sequences.concat(
        new BaseSequence<Sequence<Integer>, Iterator<Sequence<Integer>>>(
            new BaseSequence.IteratorMaker<Sequence<Integer>, Iterator<Sequence<Integer>>>()
            {
              @Override
              public Iterator<Sequence<Integer>> make()
              {
                return Arrays.asList(
                    Sequences.simple(Arrays.asList(1, 2, 3, 4)),
                    Sequences.simple(Arrays.asList(5, 6, 7, 8))
                ).iterator();
              }

              @Override
              public void cleanup(Iterator<Sequence<Integer>> iterFromMake)
              {
                ++closedCount[0];
              }
            }
        )
    );

    Assert.assertEquals(
        9,
        seq.accumulate(
            1,
            new Accumulator<Integer, Integer>()
            {
              @Override
              public Integer accumulate(Integer accumulated, Integer in)
              {
                Assert.assertEquals(accumulated, in);
                return ++accumulated;
              }
            }
        ).intValue()
    );

    Assert.assertEquals(1, closedCount[0]);

    final Yielder<Integer> yielder = seq.toYielder(
        1,
        new YieldingAccumulator<Integer, Integer>()
        {
          @Override
          public Integer accumulate(Integer accumulated, Integer in)
          {
            Assert.assertEquals(accumulated, in);
            return ++accumulated;
          }
        }
    );
    Assert.assertEquals(9, yielder.get().intValue());

    Assert.assertEquals(1, closedCount[0]);
    yielder.close();
    Assert.assertEquals(2, closedCount[0]);
  }

  @SuppressWarnings("unchecked")
  public void testAll(Iterable<List<Integer>> vals) throws IOException
  {
    final Iterable<TestSequence<Integer>> theSequences = Iterables.transform(
        vals,
        new Function<Iterable<Integer>, TestSequence<Integer>>()
        {
          @Override
          public TestSequence<Integer> apply(@Nullable Iterable<Integer> input)
          {
            return new TestSequence<Integer>(input);
          }
        }
    );

    List<TestSequence<Integer>> accumulationSeqs = Lists.newArrayList(theSequences);
    SequenceTestHelper.testAccumulation(
        "",
        new ConcatSequence<Integer>((Sequence) Sequences.simple(accumulationSeqs)),
        Lists.newArrayList(Iterables.concat(vals))
    );

    for (TestSequence<Integer> sequence : accumulationSeqs) {
      Assert.assertTrue(sequence.isClosed());
    }

    List<TestSequence<Integer>> yieldSeqs = Lists.newArrayList(theSequences);
    SequenceTestHelper.testYield(
        "",
        new ConcatSequence<Integer>((Sequence) Sequences.simple(yieldSeqs)),
        Lists.newArrayList(Iterables.concat(vals))
    );

    for (TestSequence<Integer> sequence : yieldSeqs) {
      Assert.assertTrue(sequence.isClosed());
    }
  }
}
