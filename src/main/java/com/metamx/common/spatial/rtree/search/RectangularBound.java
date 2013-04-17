package com.metamx.common.spatial.rtree.search;

import com.google.common.base.Preconditions;
import com.metamx.common.spatial.rtree.ImmutableNode;
import com.metamx.common.spatial.rtree.ImmutablePoint;

import java.util.List;

/**
 */
public class RectangularBound implements Bound
{
  private final float[] coords;
  private final float[] minCoords;
  private final float[] maxCoords;
  private final int numDims;

  public RectangularBound(float[] coords, float[] dimLens)
  {
    Preconditions.checkArgument(coords.length == dimLens.length);

    this.coords = coords;
    this.numDims = coords.length;

    this.minCoords = new float[numDims];
    this.maxCoords = new float[numDims];

    for (int i = 0; i < numDims; i++) {
      float half = dimLens[i] / 2;
      minCoords[i] = coords[i] - half;
      maxCoords[i] = coords[i] + half;
    }
  }

  @Override
  public float[] getCoordinates()
  {
    return coords;
  }

  @Override
  public int getNumDims()
  {
    return numDims;
  }

  @Override
  public boolean overlaps(ImmutableNode node)
  {
    for (int i = 0; i < numDims; i++) {
      if ((minCoords[i] >= node.getMinCoordinates()[i] && minCoords[i] <= node.getMaxCoordinates()[i]) ||
          (maxCoords[i] >= node.getMinCoordinates()[i] && maxCoords[i] <= node.getMaxCoordinates()[i])) {
        return true;
      }
    }

    return false;
  }

  @Override
  public Iterable<ImmutablePoint> filter(Iterable<ImmutablePoint> points)
  {
    return points;
  }
}
