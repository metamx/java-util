package com.metamx.common.spatial.rtree;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.metamx.common.spatial.rtree.search.RadiusBound;
import com.metamx.common.spatial.rtree.split.LinearGutmanSplitStrategy;
import junit.framework.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.Set;

/**
 */
public class ImmutableRTreeTest
{
  @Test
  public void testToAndFromByteBuffer()
  {
    RTree tree = new RTree(2, new LinearGutmanSplitStrategy(0, 50));

    tree.insert(new float[]{0, 0}, 1);
    tree.insert(new float[]{1, 1}, 2);
    tree.insert(new float[]{2, 2}, 3);
    tree.insert(new float[]{3, 3}, 4);
    tree.insert(new float[]{4, 4}, 5);

    ImmutableRTree firstTree = ImmutableRTree.newImmutableFromMutable(tree);
    ByteBuffer buffer = ByteBuffer.wrap(firstTree.toBytes());
    ImmutableRTree secondTree = new ImmutableRTree(buffer);
    Iterable<Integer> points = secondTree.search(new RadiusBound(new float[]{0, 0}, 10));
    Assert.assertTrue(Iterables.size(points) >= 5);

    Set<Integer> expected = Sets.newHashSet(1, 2, 3, 4, 5);
    for (Integer point : points) {
      Assert.assertTrue(expected.contains(point));
    }
  }


  @Test
  public void testSearchNoSplit()
  {
    RTree tree = new RTree(2, new LinearGutmanSplitStrategy(0, 50));
    tree.insert(new float[]{0, 0}, 1);
    tree.insert(new float[]{10, 10}, 10);
    tree.insert(new float[]{1, 3}, 2);
    tree.insert(new float[]{27, 34}, 20);
    tree.insert(new float[]{106, 19}, 30);
    tree.insert(new float[]{4, 2}, 3);
    tree.insert(new float[]{5, 0}, 4);
    tree.insert(new float[]{4, 72}, 40);
    tree.insert(new float[]{-4, -3}, 5);
    tree.insert(new float[]{119, -78}, 50);

    Assert.assertEquals(tree.getRoot().getChildren().size(), 10);

    ImmutableRTree searchTree = ImmutableRTree.newImmutableFromMutable(tree);
    Iterable<Integer> points = searchTree.search(new RadiusBound(new float[]{0, 0}, 5));
    Assert.assertTrue(Iterables.size(points) >= 5);

    Set<Integer> expected = Sets.newHashSet(1, 2, 3, 4, 5);
    for (Integer point : points) {
      Assert.assertTrue(expected.contains(point));
    }
  }

  @Test
  public void testSearchWithSplit()
  {
    RTree tree = new RTree(2, new LinearGutmanSplitStrategy(0, 50));
    tree.insert(new float[]{0, 0}, 1);
    tree.insert(new float[]{1, 3}, 2);
    tree.insert(new float[]{4, 2}, 3);
    tree.insert(new float[]{5, 0}, 4);
    tree.insert(new float[]{-4, -3}, 5);

    Random rand = new Random();
    for (int i = 0; i < 95; i++) {
      tree.insert(
          new float[]{(float) (rand.nextDouble() * 10 + 10.0), (float) (rand.nextDouble() * 10 + 10.0)},
          i
      );
    }

    ImmutableRTree searchTree = ImmutableRTree.newImmutableFromMutable(tree);
    Iterable<Integer> points = searchTree.search(new RadiusBound(new float[]{0, 0}, 5));
    Assert.assertTrue(Iterables.size(points) >= 5);

    Set<Integer> expected = Sets.newHashSet(1, 2, 3, 4, 5);
    for (Integer point : points) {
      Assert.assertTrue(expected.contains(point));
    }
  }

  //@Test
  public void showBenchmarks()
  {
    final int start = 1;
    final int factor = 10;
    final int end = 10000000;
    final int radius = 10;

    for (int numPoints = start; numPoints <= end; numPoints *= factor) {
      try {
        RTree tree = new RTree(2, new LinearGutmanSplitStrategy(0, 50));

        Stopwatch stopwatch = new Stopwatch().start();
        Random rand = new Random();
        for (int i = 0; i < numPoints; i++) {
          tree.insert(new float[]{(float) (rand.nextDouble() * 100), (float) (rand.nextDouble() * 100)}, i);
        }
        long stop = stopwatch.elapsedMillis();
        System.out.printf("[%,d]: insert = %,d ms%n", numPoints, stop);

        stopwatch.reset().start();
        ImmutableRTree searchTree = ImmutableRTree.newImmutableFromMutable(tree);
        stop = stopwatch.elapsedMillis();
        System.out.printf("[%,d]: size = %,d bytes%n", numPoints, searchTree.toBytes().length);
        System.out.printf("[%,d]: buildImmutable = %,d ms%n", numPoints, stop);

        stopwatch.reset().start();

        Iterable<Integer> points = searchTree.search(new RadiusBound(new float[]{50, 50}, radius));
        int count = 0;

        for (Integer point : points) {
          count++;
        }

        stop = stopwatch.elapsedMillis();
        System.out.printf("[%,d]: search = %,d points in %,d ms%n", numPoints, count, stop);
      }
      catch (Exception e) {
        throw Throwables.propagate(e);
      }
    }
  }
}
