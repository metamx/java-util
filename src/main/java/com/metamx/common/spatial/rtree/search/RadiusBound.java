package com.metamx.common.spatial.rtree.search;

import com.google.common.collect.Lists;
import com.metamx.common.spatial.rtree.Point;

import java.util.List;

/**
 */
public class RadiusBound<T> extends RectangularBound<T>
{
  private final double[] coords;
  private final double radius;

  public RadiusBound(double[] coords, double radius)
  {
    super(coords, new double[]{radius, radius});

    this.coords = coords;
    this.radius = radius;
  }

  @Override
  public List<Point<T>> filter(List<Point<T>> points)
  {
    List<Point<T>> retVal = Lists.newArrayList();

    for (Point<T> point : points) {
      double total = 0.0;

      for (int i = 0; i < coords.length; i++) {
        total += Math.pow(point.getCoords()[i] - coords[i], 2);
      }

      if (total <= Math.pow(radius, 2)) {
        retVal.add(point);
      }
    }

    return retVal;
  }
}
