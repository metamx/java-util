package com.metamx.common.spatial.rtree.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.metamx.common.spatial.rtree.ImmutablePoint;

/**
 */
public class RadiusBound extends RectangularBound
{
  private static float[] getMinCoords(float[] coords, float radius)
  {
    float[] retVal = new float[coords.length];
    for (int i = 0; i < coords.length; i++) {
      retVal[i] = coords[i] - radius;
    }
    return retVal;
  }

  private static float[] getMaxCoords(float[] coords, float radius)
  {
    float[] retVal = new float[coords.length];
    for (int i = 0; i < coords.length; i++) {
      retVal[i] = coords[i] + radius;
    }
    return retVal;
  }

  private final float[] coords;
  private final float radius;

  @JsonCreator
  public RadiusBound(
      @JsonProperty("coords") float[] coords,
      @JsonProperty("radius") float radius
  )
  {
    super(getMinCoords(coords, radius), getMaxCoords(coords, radius));

    this.coords = coords;
    this.radius = radius;
  }

  @JsonProperty
  public float[] getCoords()
  {
    return coords;
  }

  @JsonProperty
  public float getRadius()
  {
    return radius;
  }

  @Override
  public boolean contains(float[] otherCoords)
  {
    double total = 0.0;
    for (int i = 0; i < coords.length; i++) {
      total += Math.pow(otherCoords[i] - coords[i], 2);
    }

    return (total <= Math.pow(radius, 2));
  }

  @Override
  public Iterable<ImmutablePoint> filter(Iterable<ImmutablePoint> points)
  {
    return Iterables.filter(
        points,
        new Predicate<ImmutablePoint>()
        {
          @Override
          public boolean apply(ImmutablePoint point)
          {
            return contains(point.getCoords());
          }
        }
    );
  }
}
