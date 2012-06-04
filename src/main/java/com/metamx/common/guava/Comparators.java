package com.metamx.common.guava;

import org.joda.time.DateTimeComparator;
import org.joda.time.Interval;

import java.util.Comparator;

/**
 */
public class Comparators
{
  /**
   * This is a "reverse" comparator.  Positive becomes negative, negative becomes positive and 0 (equal) stays the same.
   * This was poorly named as "inverse" as it's not really inverting a true/false relationship
   * 
   * @param baseComp
   * @param <T>
   * @return
   */
  public static <T> Comparator<T> inverse(final Comparator<T> baseComp)
  {
    return new Comparator<T>()
    {
      @Override
      public int compare(T t, T t1)
      {
        return - baseComp.compare(t, t1);
      }
    };
  }

  public static <T extends Comparable> Comparator<T> comparable()
  {
    return new Comparator<T>()
    {
      @Override
      public int compare(T t, T t1)
      {
        return t.compareTo(t1);
      }
    };
  }

  private static final Comparator<Interval> INTERVAL_COMPARATOR = new Comparator<Interval>()
  {
    private DateTimeComparator dateTimeComp = DateTimeComparator.getInstance();

    @Override
    public int compare(Interval lhs, Interval rhs)
    {
      int retVal = dateTimeComp.compare(lhs.getStart(), rhs.getStart());
      if (retVal == 0) {
        retVal = dateTimeComp.compare(lhs.getEnd(), rhs.getEnd());
      }
      return retVal;
    }
  };

  public static Comparator<Interval> intervals()
  {
    return INTERVAL_COMPARATOR;
  }
}
