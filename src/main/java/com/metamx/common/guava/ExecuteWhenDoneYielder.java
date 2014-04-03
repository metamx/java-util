/*
 * Druid - a distributed column store.
 * Copyright (C) 2012, 2013, 2014  Metamarkets Group Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.metamx.common.guava;

import java.io.IOException;
import java.util.concurrent.Executor;

public class ExecuteWhenDoneYielder<T> implements Yielder<T>
{
  private final Yielder<T> baseYielder;
  private final Runnable runnable;
  private final Executor executor;

  public ExecuteWhenDoneYielder(Yielder<T> baseYielder, Runnable runnable, Executor executor)
  {
    this.baseYielder = baseYielder;
    this.runnable = runnable;
    this.executor = executor;
  }

  @Override
  public T get()
  {
    return baseYielder.get();
  }

  @Override
  public Yielder<T> next(T initValue)
  {
    return new ExecuteWhenDoneYielder<T>(baseYielder.next(initValue), runnable, executor);
  }

  @Override
  public boolean isDone()
  {
    return baseYielder.isDone();
  }

  @Override
  public void close() throws IOException
  {
    if (isDone()) {
      executor.execute(runnable);
    }
    baseYielder.close();
  }
}
