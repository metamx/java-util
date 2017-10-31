/*
 * Copyright 2016 Metamarkets Group Inc.
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
 *
 */

package com.metamx.emitter.core;

import com.google.common.primitives.UnsignedInts;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A class to accumulate simple stats of some time points. All methods are safe to use from multiple threads.
 */
public class ConcurrentTimeCounter
{
  /** Lower 32 bits for sum of {@link #add}ed times, higher 32 bits for the count */
  private final AtomicLong timeSumAndCount = new AtomicLong(0L);
  /** Lower 32 bits for the max {@link #add}ed time, 63th bit for indication if any value is added. */
  private final AtomicLong max = new AtomicLong(-1);
  /** Similar to {@link #max} */
  private final AtomicLong min = new AtomicLong(-1);

  public void add(int time)
  {
    long x = (1L << 32) | time;
    timeSumAndCount.addAndGet(x);
    updateMax(time);
    updateMin(time);
  }

  private void updateMax(int time)
  {
    long max;
    do {
      max = this.max.get();
      if (max >= 0 && ((int) max) >= time) {
        return;
      }
    } while (!this.max.compareAndSet(max, UnsignedInts.toLong(time)));
  }

  private void updateMin(int time)
  {
    long min;
    do {
      min = this.min.get();
      if (min >= 0 && ((int) min) <= time) {
        return;
      }
    } while (!this.min.compareAndSet(min, UnsignedInts.toLong(time)));
  }

  public long getTimeSumAndCountAndReset()
  {
    return timeSumAndCount.getAndSet(0L);
  }

  public int getAndResetMaxTime()
  {
    long max = this.max.getAndSet(-1);
    // If max < 0, means no times added yet, then return 0
    return max >= 0 ? (int) max : 0;
  }

  public int getAndResetMinTime()
  {
    long min = this.min.getAndSet(-1);
    // If min < 0, means no times added yet, then return 0
    return min >= 0 ? (int) min : 0;
  }

  public static int timeSum(long timeSumAndCount)
  {
    return (int) timeSumAndCount;
  }

  public static int count(long timeSumAndCount)
  {
    return (int) (timeSumAndCount >> 32);
  }
}
