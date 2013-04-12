package com.metamx.common.spatial.rtree.search;

import com.metamx.common.spatial.rtree.Node;

import java.util.List;

/**
 */
public interface SearchStrategy<T>
{
  public List<T> search(Node<T> node, com.metamx.common.spatial.rtree.search.Bound<T> bound);
}
