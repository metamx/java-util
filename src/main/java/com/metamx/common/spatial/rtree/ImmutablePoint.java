package com.metamx.common.spatial.rtree;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 */
public class ImmutablePoint extends ImmutableNode
{
  private final int entryOffset;
  private final ByteBuffer data;

  public ImmutablePoint(int numDims, int offset, ByteBuffer data)
  {
    super(numDims, offset, 0, true, data);

    entryOffset = offset + HEADER_NUM_BYTES + getCoordinateNumBytes(numDims);
    this.data = data;
  }

  public ImmutablePoint(ImmutableNode node)
  {
    super(node.getNumDims(), node.getOffset(), 0, true, node.getData());

    entryOffset = node.getOffset() + HEADER_NUM_BYTES + getCoordinateNumBytes(node.getNumDims());
    this.data = node.getData();
  }

  public int getEntry()
  {
    return data.getInt(entryOffset);
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ImmutablePoint that = (ImmutablePoint) o;

    return Arrays.equals(getMinCoordinates(), that.getMinCoordinates()) &&
           Arrays.equals(getMaxCoordinates(), that.getMaxCoordinates()) &&
           (getEntry() == that.getEntry());
  }

  @Override
  public int hashCode()
  {
    int result = getEntry();
    result = 31 * result + Arrays.hashCode(getMinCoordinates());
    result = 31 * result + Arrays.hashCode(getMaxCoordinates());
    return result;
  }
}
