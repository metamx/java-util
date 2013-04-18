package com.metamx.common.spatial.rtree.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Floats;
import com.metamx.common.spatial.rtree.ImmutablePoint;

import java.nio.ByteBuffer;

/**
 */
public class RadiusBound extends RectangularBound
{
  private final float[] coords;
  private final float radius;

  @JsonCreator
  public RadiusBound(
      @JsonProperty("coords") float[] coords,
      @JsonProperty("radius") float radius
  )
  {
    super(coords, new float[]{radius, radius});

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
  public Iterable<ImmutablePoint> filter(Iterable<ImmutablePoint> points)
  {
    return Iterables.filter(
        points,
        new Predicate<ImmutablePoint>()
        {
          @Override
          public boolean apply(ImmutablePoint point)
          {
            double total = 0.0;

            for (int i = 0; i < coords.length; i++) {
              total += Math.pow(point.getMinCoordinates()[i] - coords[i], 2);
            }

            return (total <= Math.pow(radius, 2));
          }
        }
    );
  }
}
