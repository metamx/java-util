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

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

/**
 */
public class MergeIterable<T> implements Iterable<T>
{
  private final Comparator<T> comparator;
  private final Iterable<Iterable<T>> baseIterables;

  public MergeIterable(
      Comparator<T> comparator,
      Iterable<Iterable<T>> baseIterables
  )
  {
    this.comparator = comparator;
    this.baseIterables = baseIterables;
  }

  @Override
  public Iterator<T> iterator()
  {
    final PriorityQueue<PeekingIterator<T>> pQueue = new PriorityQueue<PeekingIterator<T>>(
        16,
        new Comparator<PeekingIterator<T>>()
        {
          @Override
          public int compare(PeekingIterator<T> lhs, PeekingIterator<T> rhs)
          {
            T lhsPeek = lhs.peek();
            while (lhsPeek == null) {
              lhs.next();
              lhsPeek = lhs.peek();
            }
            T rhsPeek = rhs.peek();
            while (rhsPeek == null) {
              rhs.next();
              rhsPeek = rhs.peek();
            }

            return comparator.compare(lhsPeek, rhsPeek);
          }
        }
    );

    for (Iterable<T> baseIterable : baseIterables) {
      final PeekingIterator<T> iter = Iterators.peekingIterator(baseIterable.iterator());

      if (iter != null && iter.hasNext()) {
        pQueue.add(iter);
      }
    }

    return new Iterator<T>()
    {
      @Override
      public boolean hasNext()
      {
        return ! pQueue.isEmpty();
      }

      @Override
      public T next()
      {
        if (! hasNext()) {
          throw new NoSuchElementException();
        }

        PeekingIterator<T> retIt = pQueue.remove();
        T retVal = retIt.next();

        if (retIt.hasNext()) {
          pQueue.add(retIt);
        }

        return retVal;
      }

      @Override
      public void remove()
      {
        throw new UnsupportedOperationException();
      }
    };
  }
}
