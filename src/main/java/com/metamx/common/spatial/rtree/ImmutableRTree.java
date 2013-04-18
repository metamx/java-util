package com.metamx.common.spatial.rtree;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.metamx.common.spatial.rtree.search.Bound;
import com.metamx.common.spatial.rtree.search.GutmanSearchStrategy;
import com.metamx.common.spatial.rtree.search.SearchStrategy;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

/**
 */
public class ImmutableRTree
{
  public static ImmutableRTree newImmutableFromMutable(RTree rTree)
  {
    ByteBuffer buffer = ByteBuffer.wrap(new byte[calcNumBytes(rTree)]);

    buffer.putInt(0, rTree.getNumDims());
    makeByteBuffer(buffer, Ints.BYTES, rTree.getRoot());

    return new ImmutableRTree(buffer);
  }

  private static int makeByteBuffer(ByteBuffer buffer, int currOffset, Node node)
  {
    buffer.putInt(currOffset, currOffset);
    currOffset += Ints.BYTES;
    buffer.putInt(currOffset, node.getChildren().size());
    currOffset += Ints.BYTES;
    buffer.put(currOffset, node.isLeaf() ? (byte) 1 : (byte) 0);
    currOffset++;
    for (float v : node.getMinCoordinates()) {
      buffer.putFloat(currOffset, v);
      currOffset += Floats.BYTES;
    }
    for (float v : node.getMaxCoordinates()) {
      buffer.putFloat(currOffset, v);
      currOffset += Floats.BYTES;
    }

    if (node instanceof Point) {
      buffer.putInt(currOffset, ((Point) node).getEntry());
      currOffset += Ints.BYTES;
      return currOffset;
    }

    int childStartOffset = currOffset + node.getChildren().size() * Ints.BYTES;
    for (Node child : node.getChildren()) {
      buffer.putInt(currOffset, childStartOffset);
      childStartOffset = makeByteBuffer(buffer, childStartOffset, child);
      currOffset += Ints.BYTES;
    }

    return childStartOffset;
  }

  private static int calcNumBytes(RTree tree)
  {
    int total = Ints.BYTES;
    total += calcNodeBytes(tree.getRoot());

    return total;
  }

  private static int calcNodeBytes(Node node)
  {
    int total = 0;

    // find size of this node
    total += ImmutableNode.HEADER_NUM_BYTES + ImmutableNode.getCoordinateNumBytes(node.getNumDims());

    if (node instanceof Point) {
      total += Ints.BYTES;
      return total;
    }

    total += node.getChildren().size() * Ints.BYTES;

    // recursively find sizes of child nodes
    for (Node child : node.getChildren()) {
      total += calcNodeBytes(child);
    }

    return total;
  }

  private final int numDims;
  private final ImmutableNode root;
  private final ByteBuffer data;

  private final SearchStrategy defaultSearchStrategy = new GutmanSearchStrategy();

  public ImmutableRTree()
  {
    this.numDims = 0;
    this.data = null;
    this.root = null;
  }

  public ImmutableRTree(ByteBuffer data)
  {
    this.numDims = data.getInt(0);
    this.data = data;
    this.root = new ImmutableNode(numDims, Ints.BYTES, data);
  }

  public int size()
  {
    return data.capacity();
  }

  public int getNumDims()
  {
    return numDims;
  }

  public Iterable<Integer> search(Bound bound)
  {
    Preconditions.checkArgument(bound.getNumDims() == numDims);

    return defaultSearchStrategy.search(root, bound);
  }

  public Iterable<Integer> search(SearchStrategy strategy, Bound bound)
  {
    Preconditions.checkArgument(bound.getNumDims() == numDims);

    return strategy.search(root, bound);
  }

  public byte[] toBytes()
  {
    ByteBuffer buf = ByteBuffer.allocate(data.capacity());
    buf.put(data.asReadOnlyBuffer());
    return buf.array();
  }

  public int compareTo(ImmutableRTree other)
  {
    return data.asReadOnlyBuffer().compareTo(other.data.asReadOnlyBuffer());
  }
}
