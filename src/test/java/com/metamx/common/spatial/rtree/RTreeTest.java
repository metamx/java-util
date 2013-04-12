package com.metamx.common.spatial.rtree;

import com.google.common.collect.Sets;
import com.metamx.common.spatial.rtree.search.GutmanSearchStrategy;
import com.metamx.common.spatial.rtree.search.RadiusBound;
import com.metamx.common.spatial.rtree.split.LinearGutmanSplitStrategy;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 */
public class RTreeTest
{
  private RTree<Integer> tree;

  @Before
  public void setUp() throws Exception
  {
    tree = new RTree<Integer>(2, new LinearGutmanSplitStrategy(0, 50), new GutmanSearchStrategy<Integer>());
  }

  @Test
  public void testInsertNoSplit()
  {
    double[] elem = new double[]{5, 5};
    tree.insert(elem, 1);
    Assert.assertTrue(Arrays.equals(elem, tree.getRoot().getMinCoordinates()));
    Assert.assertTrue(Arrays.equals(elem, tree.getRoot().getMaxCoordinates()));

    tree.insert(new double[]{6, 7}, 2);
    tree.insert(new double[]{1, 3}, 3);
    tree.insert(new double[]{10, 4}, 4);
    tree.insert(new double[]{8, 2}, 5);

    Assert.assertEquals(tree.getRoot().getChildren().size(), 5);

    double[] expectedMin = new double[]{1, 2};
    double[] expectedMax = new double[]{10, 7};

    Assert.assertTrue(Arrays.equals(expectedMin, tree.getRoot().getMinCoordinates()));
    Assert.assertTrue(Arrays.equals(expectedMax, tree.getRoot().getMaxCoordinates()));
    Assert.assertEquals(tree.getRoot().getArea(), 45.0d);
  }

  @Test
  public void testInsertDuplicatesNoSplit()
  {
    tree.insert(new double[]{1, 1}, 1);
    tree.insert(new double[]{1, 1}, 1);
    tree.insert(new double[]{1, 1}, 1);

    Assert.assertEquals(tree.getRoot().getChildren().size(), 3);
  }

  @Test
  public void testSplitOccurs()
  {
    Random rand = new Random();
    for (int i = 0; i < 100; i++) {
      tree.insert(new double[]{rand.nextDouble(), rand.nextDouble()}, rand.nextInt());
    }

    Assert.assertTrue(tree.getRoot().getChildren().size() > 1);
  }

  @Test
  public void testSearchNoSplit()
  {
    tree.insert(new double[]{0, 0}, 1);
    tree.insert(new double[]{10, 10}, 10);
    tree.insert(new double[]{1, 3}, 2);
    tree.insert(new double[]{27, 34}, 20);
    tree.insert(new double[]{106, 19}, 30);
    tree.insert(new double[]{4, 2}, 3);
    tree.insert(new double[]{5, 0}, 4);
    tree.insert(new double[]{4, 72}, 40);
    tree.insert(new double[]{-4, -3}, 5);
    tree.insert(new double[]{119, -78}, 50);

    Assert.assertEquals(tree.getRoot().getChildren().size(), 10);

    List<Integer> points = tree.search(new RadiusBound<Integer>(new double[]{0, 0}, 5));
    Assert.assertEquals(points.size(), 5);

    Set<Integer> expected = Sets.newHashSet(1, 2, 3, 4, 5);
    for (Integer point : points) {
      Assert.assertTrue(expected.contains(point));
    }
  }

  @Test
  public void testSearchWithSplit()
  {
    tree.insert(new double[]{0, 0}, 1);
    tree.insert(new double[]{1, 3}, 2);
    tree.insert(new double[]{4, 2}, 3);
    tree.insert(new double[]{5, 0}, 4);
    tree.insert(new double[]{-4, -3}, 5);

    Random rand = new Random();
    for (int i = 0; i < 95; i++) {
      tree.insert(new double[]{rand.nextDouble() * 10 + 5.0, rand.nextDouble() * 10 + 5.0}, rand.nextInt());
    }

    List<Integer> points = tree.search(new RadiusBound<Integer>(new double[]{0, 0}, 5));
    Assert.assertEquals(points.size(), 5);

    Set<Integer> expected = Sets.newHashSet(1, 2, 3, 4, 5);
    for (Integer point : points) {
      Assert.assertTrue(expected.contains(point));
    }
  }
}
