package com.metamx.common.guava.nary;

import java.util.Comparator;
import java.util.Iterator;

/**
 */
public class SortedMergeIterable<InType, OutType> implements Iterable<OutType>
{
  public static <InType, OutType> SortedMergeIterable<InType, OutType> create(
      Iterable<InType> lhs,
      Iterable<InType> rhs,
      Comparator<InType> comparator,
      BinaryFn<InType, InType, OutType> fn
  )
  {
    return new SortedMergeIterable<InType, OutType>(lhs, rhs, comparator, fn);
  }

  private final Iterable<InType> lhs;
  private final Iterable<InType> rhs;
  private final Comparator<InType> comparator;
  private final BinaryFn<InType, InType, OutType> fn;

  public SortedMergeIterable(
      Iterable<InType> lhs,
      Iterable<InType> rhs,
      Comparator<InType> comparator,
      BinaryFn<InType, InType, OutType> fn
  )
  {
    this.lhs = lhs;
    this.rhs = rhs;
    this.comparator = comparator;
    this.fn = fn;
  }

  @Override
  public Iterator<OutType> iterator()
  {
    return  SortedMergeIterator.create(lhs.iterator(), rhs.iterator(), comparator, fn);
  }
}
