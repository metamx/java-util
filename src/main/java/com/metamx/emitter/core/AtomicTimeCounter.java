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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class AtomicTimeCounter
{
  private final AtomicLong state = new AtomicLong(0L);
  private final AtomicInteger max = new AtomicInteger(Integer.MIN_VALUE);
  private final AtomicInteger min = new AtomicInteger(Integer.MAX_VALUE);

  public void add(int time)
  {
    long x = (1L << 32) | time;
    state.addAndGet(x);
    updateMax(time);
    updateMin(time);
  }

  private void updateMax(int time)
  {
    int max;
    do {
      max = this.max.get();
      if (max >= time) {
        return;
      }
    } while (!this.max.compareAndSet(max, time));
  }

  private void updateMin(int time)
  {
    int min;
    do {
      min = this.min.get();
      if (min <= time) {
        return;
      }
    } while (!this.min.compareAndSet(min, time));
  }

  public long getStateAndReset()
  {
    return state.getAndSet(0L);
  }

  public int getAndResetMaxTime()
  {
    return max.getAndSet(Integer.MIN_VALUE);
  }

  public int getAndResetMinTime()
  {
    return min.getAndSet(Integer.MAX_VALUE);
  }

  public static int timeSum(long state)
  {
    return (int) state;
  }

  public static int count(long state)
  {
    return (int) (state >> 32);
  }
}
