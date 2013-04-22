package com.metamx.common.spatial.rtree;

import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;

import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * Byte layout:
 * 0 to 3 : numChildren
 * 4 : isLeaf
 * 5 to 5 + numDims * Floats.BYTES : minCoordinates
 * 5 + numDims * Floats.BYTES to 5 + 2 * numDims * Floats.BYTES : maxCoordinates
 * rest (children) : Every 4 bytes is storing an offset representing the position of a child.
 *                   This child offset is an offset from the initialOffset
 */
public class ImmutableNode
{
  public static int getCoordinateNumBytes(int numDims)
  {
    return 2 * numDims * Floats.BYTES;
  }

  public static final int HEADER_NUM_BYTES = 5;

  private final int numDims;
  private final int initialOffset;
  private final int offsetFromInitial;

  private final int numChildren;
  private final boolean isLeaf;

  private final int childrenOffset;

  private final ByteBuffer data;

  public ImmutableNode(int numDims, int initialOffset, int offsetFromInitial, ByteBuffer data)
  {
    this.numDims = numDims;
    this.initialOffset = initialOffset;
    this.offsetFromInitial = offsetFromInitial;
    this.numChildren = data.getInt(initialOffset + offsetFromInitial);
    this.isLeaf = (data.get(initialOffset + offsetFromInitial + Ints.BYTES) == 0x1);

    childrenOffset = initialOffset + offsetFromInitial + HEADER_NUM_BYTES + getCoordinateNumBytes(numDims);

    this.data = data;
  }

  public ImmutableNode(
      int numDims,
      int initialOffset,
      int offsetFromInitial,
      int numChildren,
      boolean leaf,
      ByteBuffer data
  )
  {
    this.numDims = numDims;
    this.initialOffset = initialOffset;
    this.offsetFromInitial = offsetFromInitial;
    this.numChildren = numChildren;
    this.isLeaf = leaf;

    this.childrenOffset = initialOffset + offsetFromInitial + HEADER_NUM_BYTES + getCoordinateNumBytes(numDims);

    this.data = data;
  }

  public int getInitialOffset()
  {
    return initialOffset;
  }

  public int getOffsetFromInitial()
  {
    return offsetFromInitial;
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
    return getCoords(initialOffset + offsetFromInitial + HEADER_NUM_BYTES);
  }

  public float[] getMaxCoordinates()
  {
    return getCoords(initialOffset + offsetFromInitial + HEADER_NUM_BYTES + +numDims * Floats.BYTES);
  }

  public Iterable<ImmutableNode> getChildren()
  {
    return new Iterable<ImmutableNode>()
    {
      @Override
      public Iterator<ImmutableNode> iterator()
      {
        return new Iterator<ImmutableNode>()
        {
          private volatile int count = 0;

          @Override
          public boolean hasNext()
          {
            return (count < numChildren);
          }

          @Override
          public ImmutableNode next()
          {
            return new ImmutableNode(
                numDims,
                initialOffset,
                data.getInt(childrenOffset + (count++) * Ints.BYTES),
                data
            );
          }

          @Override
          public void remove()
          {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

  public ByteBuffer getData()
  {
    return data;
  }

  private float[] getCoords(int offset)
  {
    final float[] retVal = new float[numDims];

    final ByteBuffer readOnlyBuffer = data.asReadOnlyBuffer();
    readOnlyBuffer.position(offset);
    readOnlyBuffer.asFloatBuffer().get(retVal);

    return retVal;
  }
}
