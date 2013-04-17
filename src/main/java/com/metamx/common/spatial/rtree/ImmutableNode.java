package com.metamx.common.spatial.rtree;

import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * Byte layout:
 * 0 to 3 : offset
 * 4 to 7 : numChildren
 * 8 : isLeaf
 * 9 to 9 + numDims * Floats.BYTES : minCoordinates
 * 9 + numDims * Floats.BYTES to 9 + 2 * numDims * Floats.BYTES : maxCoordinates
 * everything else : every 4 bytes is a child offset
 */
public class ImmutableNode
{
  public static int getCoordinateNumBytes(int numDims)
  {
    return 2 * numDims * Floats.BYTES;
  }

  public static final int HEADER_NUM_BYTES = 9;

  private final int numDims;
  private final int offset;
  private final int numChildren;
  private final boolean isLeaf;

  private final int childrenOffset;

  private final ByteBuffer data;

  public ImmutableNode(int numDims, int offset, ByteBuffer data)
  {
    this.numDims = numDims;
    this.offset = offset;
    this.numChildren = data.getInt(offset + 4);
    this.isLeaf = (data.get(offset + 8) == 0x1);

    childrenOffset = offset + HEADER_NUM_BYTES + getCoordinateNumBytes(numDims);

    this.data = data;
  }

  public ImmutableNode(
      int numDims,
      int offset,
      int numChildren,
      boolean leaf,
      ByteBuffer data
  )
  {
    this.numDims = numDims;
    this.offset = offset;
    this.numChildren = numChildren;
    this.isLeaf = leaf;

    this.childrenOffset = offset + HEADER_NUM_BYTES + getCoordinateNumBytes(numDims);

    this.data = data;
  }

  public int getOffset()
  {
    return offset;
  }

  public int getNumDims()
  {
    return numDims;
  }

  public int getNumChildren()
  {
    return numChildren;
  }

  public boolean isLeaf()
  {
    return isLeaf;
  }

  public float[] getMinCoordinates()
  {
    //TODO: make not suck
    float[] retVal = new float[numDims];
    for (int i = 0; i < numDims; i++) {
      retVal[i] = data.getFloat(offset + HEADER_NUM_BYTES + i * Floats.BYTES);
    }
    return retVal;
  }

  public float[] getMaxCoordinates()
  {
    float[] retVal = new float[numDims];
    for (int i = 0; i < numDims; i++) {
      retVal[i] = data.getFloat(offset + HEADER_NUM_BYTES + numDims * Floats.BYTES + i * Floats.BYTES);
    }
    return retVal;
  }

  public List<ImmutableNode> getChildren()
  {
    List<ImmutableNode> retVal = Lists.newArrayList();
    for (int i = 0; i < numChildren; i++) {
      retVal.add(new ImmutableNode(numDims, data.getInt(childrenOffset + i * Ints.BYTES), data));
    }

    return retVal;
  }

  public ByteBuffer getData()
  {
    return data;
  }
}
