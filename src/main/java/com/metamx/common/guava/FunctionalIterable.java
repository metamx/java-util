package com.metamx.common.guava;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.metamx.common.guava.nary.BinaryFn;
import com.metamx.common.guava.nary.BinaryTransformIterable;
import com.metamx.common.guava.nary.TrinaryFn;
import com.metamx.common.guava.nary.TrinaryTransformIterable;

import java.util.Iterator;

/**
 */
public class FunctionalIterable<T> implements Iterable<T>
{
  private final Iterable<T> delegate;

  public static <T> FunctionalIterable<T> create(Iterable<T> delegate)
  {
    return new FunctionalIterable<T>(delegate);
  }

  public static <T> FunctionalIterable<T> fromConcatenation(Iterable<T>... delegates)
  {
    return new FunctionalIterable<T>(Iterables.concat(delegates));
  }

  public static <T> FunctionalIterable<T> fromConcatenation(Iterable<Iterable<T>> delegates)
  {
    return new FunctionalIterable<T>(Iterables.concat(delegates));
  }

  public FunctionalIterable(
      Iterable<T> delegate
  )
  {
    this.delegate = delegate;
  }

  public Iterator<T> iterator()
  {
    return delegate.iterator();
  }

  public <RetType> FunctionalIterable<RetType> transform(Function<T, RetType> fn)
  {
    return new FunctionalIterable<RetType>(Iterables.transform(delegate, fn));
  }

  public <RetType> FunctionalIterable<RetType> transformCat(Function<T, Iterable<RetType>> fn)
  {
    return new FunctionalIterable<RetType>(Iterables.concat(Iterables.transform(delegate, fn)));
  }

  public <RetType> FunctionalIterable<RetType> keep(Function<T, RetType> fn)
  {
    return new FunctionalIterable<RetType>(Iterables.filter(Iterables.transform(delegate, fn), Predicates.notNull()));
  }

  public FunctionalIterable<T> filter(Predicate<T> pred)
  {
    return new FunctionalIterable<T>(Iterables.filter(delegate, pred));
  }

  public FunctionalIterable<T> drop(int numToDrop)
  {
    return new FunctionalIterable<T>(new DroppingIterable<T>(delegate, numToDrop));
  }

  public FunctionalIterable<T> limit(int limit)
  {
    return new FunctionalIterable<T>(Iterables.limit(delegate, limit));
  }

  public FunctionalIterable<T> concat(Iterable<T>... toConcat)
  {
    if (toConcat.length == 1) {
      return new FunctionalIterable<T>(Iterables.concat(delegate, toConcat[0]));
    }
    return new FunctionalIterable<T>(Iterables.concat(delegate, Iterables.concat(toConcat)));
  }

  public FunctionalIterable<T> concat(Iterable<Iterable<T>> toConcat)
  {
    return new FunctionalIterable<T>(Iterables.concat(delegate, Iterables.concat(toConcat)));
  }

  public <InType, RetType> FunctionalIterable<RetType> binaryTransform(
      final Iterable<InType> otherIterable, final BinaryFn<T, InType, RetType> binaryFn
  )
  {
    return new FunctionalIterable<RetType>(BinaryTransformIterable.create(delegate, otherIterable, binaryFn));
  }

  public <InType1, InType2, RetType> FunctionalIterable<RetType> trinaryTransform(
      final Iterable<InType1> iterable1,
      final Iterable<InType2> iterable2,
      final TrinaryFn<T, InType1, InType2, RetType> trinaryFn
  )
  {
    return new FunctionalIterable<RetType>(TrinaryTransformIterable.create(delegate, iterable1, iterable2, trinaryFn));
  }
}
