package com.metamx.common.spatial.rtree.search;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.metamx.common.spatial.rtree.ImmutableNode;
import com.metamx.common.spatial.rtree.ImmutablePoint;

/**
 */
public class GutmanSearchStrategy implements com.metamx.common.spatial.rtree.search.SearchStrategy
{
  @Override
  public Iterable<Integer> search(ImmutableNode node, Bound bound)
  {
    return Iterables.transform(
        innerSearch(node, bound),
        new Function<ImmutablePoint, Integer>()
        {
          @Override
          public Integer apply(ImmutablePoint immutablePoint)
          {
            return immutablePoint.getEntry();
          }
        }
    );
  }

  public Iterable<ImmutablePoint> innerSearch(ImmutableNode node, final Bound bound)
  {
    if (node.isLeaf()) {
      return bound.filter(
          Iterables.transform(
              node.getChildren(),
              new Function<ImmutableNode, ImmutablePoint>()
              {
                @Override
                public ImmutablePoint apply(ImmutableNode tNode)
                {
                  return new ImmutablePoint(tNode);
                }
              }
          )
      );
    } else {
      return Iterables.concat(
          Iterables.transform(
              Iterables.filter(
                  node.getChildren(),
                  new Predicate<ImmutableNode>()
                  {
                    @Override
                    public boolean apply(ImmutableNode child)
                    {
                      return bound.overlaps(child);
                    }
                  }
              ),
              new Function<ImmutableNode, Iterable<ImmutablePoint>>()
              {
                @Override
                public Iterable<ImmutablePoint> apply(ImmutableNode child)
                {
                  return innerSearch(child, bound);
                }
              }
          )
      );
    }
  }
}