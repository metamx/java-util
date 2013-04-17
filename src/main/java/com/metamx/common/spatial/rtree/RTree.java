package com.metamx.common.spatial.rtree;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.metamx.common.spatial.rtree.search.Bound;
import com.metamx.common.spatial.rtree.search.GutmanSearchStrategy;
import com.metamx.common.spatial.rtree.search.SearchStrategy;
import com.metamx.common.spatial.rtree.split.LinearGutmanSplitStrategy;
import com.metamx.common.spatial.rtree.split.SplitStrategy;

import java.util.Arrays;
import java.util.List;

/**
 * This RTree only takes integer entries because serde is hard.
 */
public class RTree
{
  private final int numDims;
  private final SplitStrategy splitStrategy;

  private Node root;

  private volatile int size;

  public RTree()
  {
    this(0, new LinearGutmanSplitStrategy(0, 50));
  }

  public RTree(int numDims, SplitStrategy splitStrategy)
  {
    this.numDims = numDims;
    this.splitStrategy = splitStrategy;
    this.root = buildRoot(true);
  }

  public void insert(float[] coords, int entry)
  {
    Preconditions.checkArgument(coords.length == numDims);

    Point point = new Point(coords, entry);
    Node node = chooseLeaf(root, point);
    node.addChild(point);
    point.setParent(node);

    if (splitStrategy.needToSplit(node)) {
      Node[] groups = splitStrategy.split(node);
      adjustTree(groups[0], groups[1]);
    } else {
      adjustTree(node, null);
    }

    size++;
  }

  public boolean delete(double[] coords, int entry)
  {
    throw new UnsupportedOperationException();
  }

  public int getSize()
  {
    return size;
  }

  public int getNumDims()
  {
    return numDims;
  }

  public SplitStrategy getSplitStrategy()
  {
    return splitStrategy;
  }

  public Node getRoot()
  {
    return root;
  }

  private Node buildRoot(boolean isLeaf)
  {
    float[] initMinCoords = new float[numDims];
    float[] initMaxCoords = new float[numDims];
    Arrays.fill(initMinCoords, -Float.MAX_VALUE);
    Arrays.fill(initMaxCoords, Float.MAX_VALUE);

    return new Node(initMinCoords, initMaxCoords, isLeaf);
  }

  private Node chooseLeaf(Node node, Point point)
  {
    if (node.isLeaf()) {
      return node;
    }

    double minCost = Double.MAX_VALUE;
    Node optimal = null;
    for (Node child : node.getChildren()) {
      double cost = RTreeUtils.getExpansionCost(child, point);
      if (cost < minCost) {
        minCost = cost;
        optimal = child;
      } else if (cost == minCost) {
        // Resolve ties by choosing the entry with the rectangle of smallest area
        if (child.getArea() < optimal.getArea()) {
          optimal = child;
        }
      }
    }

    return chooseLeaf(optimal, point);
  }

  private void adjustTree(Node n, Node nn)
  {
    // special case for root
    if (n == root) {
      if (nn != null) {
        root = buildRoot(false);
        root.addChild(n);
        root.addChild(nn);
        n.setParent(root);
        nn.setParent(root);
      }
      root.enclose();
      return;
    }

    n.enclose();

    if (nn != null) {
      nn.enclose();

      if (splitStrategy.needToSplit(n.getParent())) {
        Node[] groups = splitStrategy.split(n.getParent());
        adjustTree(groups[0], groups[1]);
      }
    }

    if (n.getParent() != null) {
      adjustTree(n.getParent(), null);
    }
  }
}
