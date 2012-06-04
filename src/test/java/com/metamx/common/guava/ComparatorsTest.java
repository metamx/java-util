package com.metamx.common.guava;

import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;

/**
 */
public class ComparatorsTest
{
  @Test
  public void testInverse() throws Exception
  {
    Comparator<Integer> normal = Comparators.<Integer>comparable();
    Comparator<Integer> inverted = Comparators.inverse(normal);

    Assert.assertEquals(-1, normal.compare(0, 1));
    Assert.assertEquals(1, normal.compare(1, 0));
    Assert.assertEquals(0, normal.compare(1, 1));
    Assert.assertEquals(1, inverted.compare(0, 1));
    Assert.assertEquals(-1, inverted.compare(1, 0));
    Assert.assertEquals(0, inverted.compare(1, 1));
  }

  @Test
  public void testIntervals() throws Exception
  {
    Comparator<Interval> comp = Comparators.intervals();

    Assert.assertEquals(0, comp.compare(new Interval("P1d/2011-04-02"), new Interval("2011-04-01/2011-04-02")));
    Assert.assertEquals(-1, comp.compare(new Interval("2011-03-31/2011-04-02"), new Interval("2011-04-01/2011-04-02")));
    Assert.assertEquals(1, comp.compare(new Interval("2011-04-01/2011-04-02"), new Interval("2011-03-31/2011-04-02")));
    Assert.assertEquals(1, comp.compare(new Interval("2011-04-01/2011-04-03"), new Interval("2011-04-01/2011-04-02")));
    Assert.assertEquals(-1, comp.compare(new Interval("2011-04-01/2011-04-03"), new Interval("2011-04-01/2011-04-04")));

    Interval[] intervals = new Interval[]{
        new Interval("2011-04-01T18/2011-04-02T13"),
        new Interval("2011-04-01/2011-04-03"),
        new Interval("2011-04-01/2011-04-04"),
        new Interval("2011-04-02/2011-04-04"),
        new Interval("2011-04-01/2011-04-02"),
        new Interval("2011-04-02/2011-04-03"),
        new Interval("2011-04-02/2011-04-03T06")
    };
    Arrays.sort(intervals, comp);

    Assert.assertArrayEquals(
        new Interval[]{
            new Interval("2011-04-01/2011-04-02"),
            new Interval("2011-04-01/2011-04-03"),
            new Interval("2011-04-01/2011-04-04"),
            new Interval("2011-04-01T18/2011-04-02T13"),
            new Interval("2011-04-02/2011-04-03"),
            new Interval("2011-04-02/2011-04-03T06"),
            new Interval("2011-04-02/2011-04-04"),
        },
        intervals
    );
  }
}
