package com.metamx.common.spatial.rtree.search;

import com.metamx.common.spatial.rtree.Node;
import com.metamx.common.spatial.rtree.Point;

import java.util.List;

/**
 */
public interface Bound<T>
{
  public double[] getCoordinates();

  public int getNumDims();

  public boolean overlaps(Node node);

  public List<Point<T>> filter(List<Point<T>> points);
}
