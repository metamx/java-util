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

package com.metamx.common.lifecycle;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.metamx.common.logger.Logger;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 */
public class Lifecycle
{
  private static final Logger log = new Logger(Lifecycle.class);

  private final Map<Stage, Deque<Handler>> handlers;

  public static enum Stage
  {
    NORMAL,
    LAST
  }

  public Lifecycle()
  {
    handlers = Maps.newHashMap();
    for (Stage stage : Stage.values()) {
      handlers.put(stage, Lists.<Handler>newLinkedList());
    }
  }

  public <T> T addManagedInstance(T o)
  {
    addHandler(new AnnotationBasedHandler(o));
    return o;
  }

  public <T> T addManagedInstance(T o, Stage stage)
  {
    addHandler(new AnnotationBasedHandler(o), stage);
    return o;
  }

  public <T> T addStartCloseInstance(T o)
  {
    addHandler(new StartCloseHandler(o));
    return o;
  }

  public <T> T addStartCloseInstance(T o, Stage stage)
  {
    addHandler(new StartCloseHandler(o), stage);
    return o;
  }

  public void addHandler(Handler handler)
  {
    addHandler(handler, Stage.NORMAL);
  }

  public void addHandler(Handler handler, Stage stage)
  {
    handlers.get(stage).addLast(handler);
  }

  public void start() throws Exception
  {
    for (Stage stage : stagesOrdered()) {
      for (Handler handler : handlers.get(stage)) {
        handler.start();
      }
    }
  }

  public void stop()
  {
    for (Stage stage : Lists.reverse(stagesOrdered())) {
      final Iterator<Handler> iter = handlers.get(stage).descendingIterator();
      while (iter.hasNext()) {
        iter.next().stop();
      }
    }
  }

  public void join() throws InterruptedException
  {
    Runtime.getRuntime().addShutdownHook(
        new Thread(
            new Runnable()
            {
              @Override
              public void run()
              {
                log.info("Running shutdown hook");
                stop();
              }
            }
        )
    );

    Thread.currentThread().join();
  }

  private static List<Stage> stagesOrdered()
  {
    return Arrays.asList(Stage.values());
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
