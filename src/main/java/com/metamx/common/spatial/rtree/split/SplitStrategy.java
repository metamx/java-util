package com.metamx.common.spatial.rtree.split;

import com.metamx.common.spatial.rtree.Node;

/**
 */
public interface SplitStrategy
{
  public boolean needToSplit(Node node);

  public Node[] split(Node node);
}
