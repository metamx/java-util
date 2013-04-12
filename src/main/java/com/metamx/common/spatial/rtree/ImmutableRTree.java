package com.metamx.common.spatial.rtree;

import com.metamx.common.spatial.rtree.search.Bound;
import com.metamx.common.spatial.rtree.search.SearchStrategy;
import com.metamx.common.spatial.rtree.split.SplitStrategy;

import java.util.List;

/**
 */
public class ImmutableRTree<T> extends RTree<T>
{
  public static <T> ImmutableRTree<T> fromMutableRTree(RTree<T> rTree)
  {
    return new ImmutableRTree<T>(
        rTree.getNumDims(),
        rTree.getSplitStrategy(),
        rTree.getSearchStrategy(),
        rTree.getRoot(),
        rTree.getSize()
    );
  }

  public static <T> ImmutableRTree<T> fromByteArray(byte[] data)
  {
    // TODO
    return new ImmutableRTree<T>(0, null, null, null, 0);
  }

  public ImmutableRTree(
      int numDims, SplitStrategy splitStrategy, SearchStrategy<T> searchStrategy, Node<T> root, int size
  )
  {
    super(
        numDims,
        splitStrategy,
        searchStrategy,
        root,
        size
    );
  }

  @Override
  public void insert(double[] coords, T entry)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getSize()
  {
    return super.getSize();
  }

  @Override
  public int getNumDims()
  {
    return super.getNumDims();
  }

  @Override
  public List<T> search(Bound<T> bound)
  {
    return super.search(bound);
  }

  public byte[] toByteArray()
  {
    // TODO
    return new byte[0];
  }
}
