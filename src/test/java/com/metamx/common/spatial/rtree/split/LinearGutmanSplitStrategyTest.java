package com.metamx.common.spatial.rtree.split;

import com.metamx.common.spatial.rtree.Node;
import com.metamx.common.spatial.rtree.Point;
import com.metamx.common.spatial.rtree.RTreeUtils;
import junit.framework.Assert;
import org.junit.Test;

/**
 */
public class LinearGutmanSplitStrategyTest
{
  @Test
  public void testPickSeeds() throws Exception
  {
    LinearGutmanSplitStrategy strategy = new LinearGutmanSplitStrategy(0, 50);
    Node<Integer> node = new Node<Integer>(new double[2], new double[2], true);

    node.addChild(new Point<Integer>(new double[]{3, 7}, 1));
    node.addChild(new Point<Integer>(new double[]{1, 6}, 1));
    node.addChild(new Point<Integer>(new double[]{9, 8}, 1));
    node.addChild(new Point<Integer>(new double[]{2, 5}, 1));
    node.addChild(new Point<Integer>(new double[]{4, 4}, 1));
    RTreeUtils.enclose(node);

    Node[] groups = strategy.split(node);
    Assert.assertEquals(groups[0].getMinCoordinates()[0], 9.0);
    Assert.assertEquals(groups[0].getMinCoordinates()[1], 8.0);
    Assert.assertEquals(groups[1].getMinCoordinates()[0], 1.0);
    Assert.assertEquals(groups[1].getMinCoordinates()[1], 4.0);
  }
}
