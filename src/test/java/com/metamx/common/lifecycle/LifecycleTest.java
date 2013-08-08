package com.metamx.common.lifecycle;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 */
public class LifecycleTest
{
  @Test
  public void testSanity() throws Exception
  {
    Lifecycle lifecycle = new Lifecycle();

    List<Integer> startOrder = Lists.newArrayList();
    List<Integer> stopOrder = Lists.newArrayList();

    lifecycle.addManagedInstance(new ObjectToBeLifecycled(0, startOrder, stopOrder));
    lifecycle.addManagedInstance(new ObjectToBeLifecycled(1, startOrder, stopOrder), Lifecycle.Stage.NORMAL);
    lifecycle.addManagedInstance(new ObjectToBeLifecycled(2, startOrder, stopOrder), Lifecycle.Stage.NORMAL);
    lifecycle.addManagedInstance(new ObjectToBeLifecycled(3, startOrder, stopOrder), Lifecycle.Stage.LAST);
    lifecycle.addStartCloseInstance(new ObjectToBeLifecycled(4, startOrder, stopOrder));
    lifecycle.addManagedInstance(new ObjectToBeLifecycled(5, startOrder, stopOrder));
    lifecycle.addStartCloseInstance(new ObjectToBeLifecycled(6, startOrder, stopOrder), Lifecycle.Stage.LAST);
    lifecycle.addManagedInstance(new ObjectToBeLifecycled(7, startOrder, stopOrder));

    final List<Integer> expectedOrder = Arrays.asList(0, 1, 2, 4, 5, 7, 3, 6);

    lifecycle.start();

    Assert.assertEquals(8, startOrder.size());
    Assert.assertEquals(0, stopOrder.size());
    Assert.assertEquals(expectedOrder, startOrder);

    lifecycle.stop();

    Assert.assertEquals(8, startOrder.size());
    Assert.assertEquals(8, stopOrder.size());
    Assert.assertEquals(Lists.reverse(expectedOrder), stopOrder);
  }

  @Test
  public void testAddToLifecycleInStartMethod() throws Exception
  {
    final Lifecycle lifecycle = new Lifecycle();

    final List<Integer> startOrder = Lists.newArrayList();
    final List<Integer> stopOrder = Lists.newArrayList();

    lifecycle.addManagedInstance(new ObjectToBeLifecycled(0, startOrder, stopOrder));
    lifecycle.addHandler(
        new Lifecycle.Handler()
        {
          @Override
          public void start() throws Exception
          {
            lifecycle.addMaybeStartManagedInstance(
                new ObjectToBeLifecycled(1, startOrder, stopOrder), Lifecycle.Stage.NORMAL
            );
            lifecycle.addMaybeStartManagedInstance(
                new ObjectToBeLifecycled(2, startOrder, stopOrder), Lifecycle.Stage.NORMAL
            );
            lifecycle.addMaybeStartManagedInstance(
                new ObjectToBeLifecycled(3, startOrder, stopOrder), Lifecycle.Stage.LAST
            );
            lifecycle.addMaybeStartStartCloseInstance(new ObjectToBeLifecycled(4, startOrder, stopOrder));
            lifecycle.addMaybeStartManagedInstance(new ObjectToBeLifecycled(5, startOrder, stopOrder));
            lifecycle.addMaybeStartStartCloseInstance(
                new ObjectToBeLifecycled(6, startOrder, stopOrder), Lifecycle.Stage.LAST
            );
            lifecycle.addMaybeStartManagedInstance(new ObjectToBeLifecycled(7, startOrder, stopOrder));
          }

          @Override
          public void stop()
          {

          }
        }
    );

    final List<Integer> expectedOrder = Arrays.asList(0, 1, 2, 4, 5, 7, 3, 6);

    lifecycle.start();

    Assert.assertEquals(expectedOrder, startOrder);
    Assert.assertEquals(0, stopOrder.size());

    lifecycle.stop();

    Assert.assertEquals(expectedOrder, startOrder);
    Assert.assertEquals(Lists.reverse(expectedOrder), stopOrder);
  }

  public static class ObjectToBeLifecycled
  {
    private final int id;
    private final List<Integer> orderOfStarts;
    private final List<Integer> orderOfStops;

    public ObjectToBeLifecycled(
        int id,
        List<Integer> orderOfStarts,
        List<Integer> orderOfStops
    )
    {
      this.id = id;
      this.orderOfStarts = orderOfStarts;
      this.orderOfStops = orderOfStops;
    }

    @LifecycleStart
    public void start()
    {
      orderOfStarts.add(id);
    }

    @LifecycleStop
    public void close()
    {
      orderOfStops.add(id);
    }
  }
}
