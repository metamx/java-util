/*
 * Copyright 2011 Metamarkets Group Inc.
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

package com.metamx.common.lifecycle;

import com.metamx.common.logger.Logger;

import java.lang.reflect.Method;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

/**
 */
public class Lifecycle
{
  private final Deque<Handler> handlers = new LinkedList<Handler>();

  public <T> T addManagedInstance(T o)
  {
    addHandler(new AnnotationBasedHandler(o));
    return o;
  }

  public <T> T addStartCloseInstance(T o)
  {
    addHandler(new StartCloseHandler(o));
    return o;
  }

  public void addHandler(Handler handler)
  {
    handlers.addLast(handler);
  }

  public void start() throws Exception
  {
    for (Handler handler : handlers) {
      handler.start();
    }
  }

  public void stop()
  {
    final Iterator<Handler> iter = handlers.descendingIterator();
    while (iter.hasNext()) {
      iter.next().stop();
    }
  }

  public static interface Handler
  {
    public void start() throws Exception;
    public void stop();
  }

  private static class AnnotationBasedHandler implements Handler
  {
    private static final Logger log = new Logger(AnnotationBasedHandler.class);

    private final Object o;

    public AnnotationBasedHandler(Object o)
    {
      this.o = o;
    }

    @Override
    public void start() throws Exception
    {
      for (Method method : o.getClass().getMethods()) {
        if (method.getAnnotation(LifecycleStart.class) != null) {
          log.info("Invoking start method[%s] on object[%s].", method, o);
          method.invoke(o);
        }
      }
    }

    @Override
    public void stop()
    {
      for (Method method : o.getClass().getMethods()) {
        if (method.getAnnotation(LifecycleStop.class) != null) {
          log.info("Invoking stop method[%s] on object[%s].", method, o);
          try {
            method.invoke(o);
          }
          catch (Exception e) {
            log.error(e, "Exception when stopping method[%s] on object[%s]", method, o);
          }
        }
      }
    }
  }

  private static class StartCloseHandler implements Handler
  {
    private static final Logger log = new Logger(StartCloseHandler.class);

    private final Object o;
    private final Method startMethod;
    private final Method stopMethod;

    public StartCloseHandler(Object o)
    {
      this.o = o;
      try {
        startMethod = o.getClass().getMethod("start");
        stopMethod = o.getClass().getMethod("close");
      }
      catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    }


    @Override
    public void start() throws Exception
    {
      log.info("Starting object[%s]", o);
      startMethod.invoke(o);
    }

    @Override
    public void stop()
    {
      log.info("Stopping object[%s]", o);
      try {
        stopMethod.invoke(o);
      }
      catch (Exception e) {
        log.error(e, "Unable to invoke stopMethod() on %s", o.getClass());
      }
    }
  }
}
