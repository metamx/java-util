package com.metamx.common.spatial.rtree;

import com.metamx.common.spatial.rtree.split.LinearGutmanSplitStrategy;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

/**
 */
public class RTreeTest
{
  private RTree tree;

  @Before
  public void setUp() throws Exception
  {
    tree = new RTree(2, new LinearGutmanSplitStrategy(0, 50));
  }

  @Test
  public void testInsertNoSplit()
  {
    float[] elem = new float[]{5, 5};
    tree.insert(elem, 1);
    Assert.assertTrue(Arrays.equals(elem, tree.getRoot().getMinCoordinates()));
    Assert.assertTrue(Arrays.equals(elem, tree.getRoot().getMaxCoordinates()));

    tree.insert(new float[]{6, 7}, 2);
    tree.insert(new float[]{1, 3}, 3);
    tree.insert(new float[]{10, 4}, 4);
    tree.insert(new float[]{8, 2}, 5);

    Assert.assertEquals(tree.getRoot().getChildren().size(), 5);

    float[] expectedMin = new float[]{1, 2};
    float[] expectedMax = new float[]{10, 7};

    Assert.assertTrue(Arrays.equals(expectedMin, tree.getRoot().getMinCoordinates()));
    Assert.assertTrue(Arrays.equals(expectedMax, tree.getRoot().getMaxCoordinates()));
    Assert.assertEquals(tree.getRoot().getArea(), 45.0d);
  }

  @Test
  public void testInsertDuplicatesNoSplit()
  {
    tree.insert(new float[]{1, 1}, 1);
    tree.insert(new float[]{1, 1}, 1);
    tree.insert(new float[]{1, 1}, 1);

    Assert.assertEquals(tree.getRoot().getChildren().size(), 3);
  }

  @Test
  public void testSplitOccurs()
  {
    Random rand = new Random();
    for (int i = 0; i < 100; i++) {
      tree.insert(new float[]{rand.nextFloat(), rand.nextFloat()}, rand.nextInt());
    }

    Assert.assertTrue(tree.getRoot().getChildren().size() > 1);
  }
}
