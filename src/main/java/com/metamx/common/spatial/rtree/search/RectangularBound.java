package com.metamx.common.spatial.rtree.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Floats;
import com.metamx.common.spatial.rtree.ImmutableNode;
import com.metamx.common.spatial.rtree.ImmutablePoint;

import java.nio.ByteBuffer;

/**
 */
public class RectangularBound implements Bound
{
  private static final byte CACHE_TYPE_ID = 0x0;

  private final float[] minCoords;
  private final float[] maxCoords;
  private final int numDims;

  @JsonCreator
  public RectangularBound(
      @JsonProperty("minCoords") float[] minCoords,
      @JsonProperty("maxCoords") float[] maxCoords
  )
  {
    Preconditions.checkArgument(minCoords.length == maxCoords.length);

    this.numDims = minCoords.length;

    this.minCoords = minCoords;
    this.maxCoords = maxCoords;
  }

  @JsonProperty
  public float[] getMinCoords()
  {
    return minCoords;
  }

  @JsonProperty
  public float[] getMaxCoords()
  {
    return maxCoords;
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
    ByteBuffer minCoordsBuffer = ByteBuffer.allocate(minCoords.length * Floats.BYTES);
    minCoordsBuffer.asFloatBuffer().put(minCoords);
    final byte[] minCoordsCacheKey = minCoordsBuffer.array();

    ByteBuffer maxCoordsBuffer = ByteBuffer.allocate(maxCoords.length * Floats.BYTES);
    maxCoordsBuffer.asFloatBuffer().put(maxCoords);
    final byte[] maxCoordsCacheKey = maxCoordsBuffer.array();

    final ByteBuffer cacheKey = ByteBuffer.allocate(1 + minCoordsCacheKey.length + maxCoordsCacheKey.length)
                                          .put(minCoordsCacheKey)
                                          .put(maxCoordsCacheKey)
                                          .put(CACHE_TYPE_ID);
    return cacheKey.array();
  }
}
