package com.metamx.common.spatial.rtree.search;

import com.google.common.base.Preconditions;
import com.metamx.common.spatial.rtree.Node;
import com.metamx.common.spatial.rtree.Point;

import java.util.List;

/**
 */
public class RectangularBound<T> implements com.metamx.common.spatial.rtree.search.Bound<T>
{
  private final double[] coords;
  private final double[] minCoords;
  private final double[] maxCoords;
  private final int numDims;

  public RectangularBound(double[] coords, double[] dimLens)
  {
    Preconditions.checkArgument(coords.length == dimLens.length);

    this.coords = coords;
    this.numDims = coords.length;

    this.minCoords = new double[numDims];
    this.maxCoords = new double[numDims];

    for (int i = 0; i < numDims; i++) {
      double half = dimLens[i] / 2;
      minCoords[i] = coords[i] - half;
      maxCoords[i] = coords[i] + half;
    }
  }

  @Override
  public double[] getCoordinates()
  {
    return coords;
  }

  @Override
  public int getNumDims()
  {
    return numDims;
  }

  @Override
  public boolean overlaps(Node node)
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
  public List<Point<T>> filter(List<Point<T>> points)
  {
    return points;
  }
}
