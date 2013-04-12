package com.metamx.common.spatial.rtree.search;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.metamx.common.spatial.rtree.Node;
import com.metamx.common.spatial.rtree.Point;

import java.util.List;

/**
 */
public class GutmanSearchStrategy<T> implements com.metamx.common.spatial.rtree.search.SearchStrategy<T>
{
  @Override
  public List<T> search(Node<T> node, com.metamx.common.spatial.rtree.search.Bound<T> bound)
  {
    return Lists.transform(
        innerSearch(node, bound),
        new Function<Point<T>, T>()
        {
          @Override
          public T apply(Point<T> tPoint)
          {
            return tPoint.getEntry();
          }
        }
    );
  }

  public List<Point<T>> innerSearch(Node<T> node, com.metamx.common.spatial.rtree.search.Bound<T> bound)
  {
    List<Point<T>> points = Lists.newArrayList();
    if (node.isLeaf()) {
      points.addAll(
          bound.filter(
              Lists.transform(
                  node.getChildren(),
                  new Function<Node<T>, Point<T>>()
                  {
                    @Override
                    public Point<T> apply(Node<T> tNode)
                    {
                      return (Point<T>) tNode;
                    }
                  }
              )
          )
      );
    } else {
      for (Node<T> child : node.getChildren()) {
        if (bound.overlaps(child)) {
          points.addAll(innerSearch(child, bound));
        }
      }
    }

    return points;
  }
}