package com.metamx.common.spatial.rtree.search;

import com.metamx.common.spatial.rtree.ImmutableNode;
import com.metamx.common.spatial.rtree.ImmutablePoint;

/**
 */
public interface Bound
{
  public float[] getCoordinates();

  public int getNumDims();

  public boolean overlaps(ImmutableNode node);

  public Iterable<ImmutablePoint> filter(Iterable<ImmutablePoint> points);
}
