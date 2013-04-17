package com.metamx.common.spatial.rtree.search;

import com.metamx.common.spatial.rtree.ImmutableNode;

import java.util.Set;

/**
 */
public interface SearchStrategy
{
  public Iterable<Integer> search(ImmutableNode node, Bound bound);
}
