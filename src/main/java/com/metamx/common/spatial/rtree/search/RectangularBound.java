package com.metamx.common.spatial.rtree.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Floats;
import com.metamx.common.spatial.rtree.ImmutableNode;
import com.metamx.common.spatial.rtree.ImmutablePoint;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;

/**
 */
public class RectangularBound implements Bound
{
  private static final byte CACHE_TYPE_ID = 0x0;

  private final float[] coords;
  private final float[] dimLens;
  private final float[] minCoords;
  private final float[] maxCoords;
  private final int numDims;

  @JsonCreator
  public RectangularBound(
      @JsonProperty("coords") float[] coords,
      @JsonProperty("dimLens") float[] dimLens
  )
  {
    Preconditions.checkArgument(coords.length == dimLens.length);

    this.coords = coords;
    this.dimLens = dimLens;
    this.numDims = coords.length;

    this.minCoords = new float[numDims];
    this.maxCoords = new float[numDims];

    for (int i = 0; i < numDims; i++) {
      float half = dimLens[i] / 2;
      minCoords[i] = coords[i] - half;
      maxCoords[i] = coords[i] + half;
    }
  }

  @JsonProperty
  @Override
  public float[] getCoordinates()
  {
    return coords;
  }

  @JsonProperty
  public float[] getDimLens()
  {
    return dimLens;
  }

  @Override
  public int getNumDims()
  {
    return numDims;
  }

  @Override
  public boolean overlaps(ImmutableNode node)
  {
    final float[] nodeMinCoords = node.getMinCoordinates();
    final float[] nodeMaxCoords = node.getMaxCoordinates();

    for (int i = 0; i < numDims; i++) {
      if (nodeMaxCoords[i] < minCoords[i] || nodeMinCoords[i] > maxCoords[i]) {
        return false;
      }
    }

    return true;
  }

  @Override
  public boolean contains(float[] coords)
  {
    for (int i = 0; i < numDims; i++) {
      if (coords[i] < minCoords[i] || coords[i] > maxCoords[i]) {
        return false;
      }
    }

    return true;
  }

  @Override
  public Iterable<ImmutablePoint> filter(Iterable<ImmutablePoint> points)
  {
    return Iterables.filter(
        points,
        new Predicate<ImmutablePoint>()
        {
          @Override
          public boolean apply(ImmutablePoint immutablePoint)
          {
            return contains(immutablePoint.getCoords());
          }
        }
    );
  }

  @Override
  public byte[] getCacheKey()
  {
    ByteBuffer coordsBuffer = ByteBuffer.allocate(coords.length * Floats.BYTES);
    coordsBuffer.asFloatBuffer().put(coords);
    final byte[] coordsCacheKey = coordsBuffer.array();

    ByteBuffer dimLensBuffer = ByteBuffer.allocate(dimLens.length * Floats.BYTES);
    dimLensBuffer.asFloatBuffer().put(dimLens);
    final byte[] dimLensCacheKey = dimLensBuffer.array();

    final ByteBuffer cacheKey = ByteBuffer.allocate(1 + coordsCacheKey.length + dimLensCacheKey.length)
                                          .put(coordsCacheKey)
                                          .put(dimLensCacheKey)
                                          .put(CACHE_TYPE_ID);
    return cacheKey.array();
  }
}
