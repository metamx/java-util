package com.metamx.common.spatial.rtree;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 */
public class ImmutablePoint extends ImmutableNode
{
  private final int entryOffset;
  private final ByteBuffer data;

  public ImmutablePoint(int numDims, int initialOffset, int offset, ByteBuffer data)
  {
    super(numDims, initialOffset, offset, 0, true, data);

    entryOffset = offset + HEADER_NUM_BYTES + getCoordinateNumBytes(numDims);
    this.data = data;
  }

  public ImmutablePoint(ImmutableNode node)
  {
    super(node.getNumDims(), node.getInitialOffset(), node.getOffsetFromInitial(), 0, true, node.getData());

    entryOffset = node.getInitialOffset() +
                  node.getOffsetFromInitial()
                  + HEADER_NUM_BYTES
                  + getCoordinateNumBytes(node.getNumDims());
    this.data = node.getData();
  }

  public int getEntry()
  {
    return data.getInt(entryOffset);
  }
}
