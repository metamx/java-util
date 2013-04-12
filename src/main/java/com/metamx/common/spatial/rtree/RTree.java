package com.metamx.common.spatial.rtree;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.metamx.common.spatial.rtree.search.Bound;
import com.metamx.common.spatial.rtree.search.SearchStrategy;
import com.metamx.common.spatial.rtree.split.SplitStrategy;

import java.util.Arrays;
import java.util.List;

/**
 */
public class RTree<T>
{
  private final int numDims;
  private final SplitStrategy splitStrategy;
  private final SearchStrategy<T> searchStrategy;

  private Node<T> root;

  private volatile int size;

  public RTree(int numDims, SplitStrategy splitStrategy, SearchStrategy<T> searchStrategy)
  {
    this.numDims = numDims;
    this.splitStrategy = splitStrategy;
    this.searchStrategy = searchStrategy;
    this.root = buildRoot(true);
  }

  public RTree(int numDims, SplitStrategy splitStrategy, SearchStrategy<T> searchStrategy, Node<T> root, int size)
  {
    this.numDims = numDims;
    this.splitStrategy = splitStrategy;
    this.searchStrategy = searchStrategy;
    this.root = root;
    this.size = size;
  }

  public void insert(double[] coords, T entry)
  {
    Preconditions.checkArgument(coords.length == numDims);

    Point<T> point = new Point<T>(coords, entry);
    Node<T> node = chooseLeaf(root, point);
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

  public boolean delete(double[] coords, T entry)
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

  public SearchStrategy<T> getSearchStrategy()
  {
    return searchStrategy;
  }

  public Node<T> getRoot()
  {
    return root;
  }

  public List<T> search(Bound<T> bound)
  {
    Preconditions.checkArgument(bound.getNumDims() == numDims);

    return searchStrategy.search(root, bound);
  }

  private Node<T> buildRoot(boolean isLeaf)
  {
    double[] initMinCoords = new double[numDims];
    double[] initMaxCoords = new double[numDims];
    Arrays.fill(initMinCoords, Double.MIN_VALUE);
    Arrays.fill(initMaxCoords, Double.MAX_VALUE);

    return new Node<T>(initMinCoords, initMaxCoords, isLeaf);
  }

  private Node<T> chooseLeaf(Node<T> node, Point point)
  {
    if (node.isLeaf()) {
      return node;
    }

    double minCost = Double.MAX_VALUE;
    Node<T> optimal = null;
    for (Node<T> child : node.getChildren()) {
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

  private void adjustTree(Node<T> n, Node<T> nn)
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
      RTreeUtils.enclose(root);
      return;
    }

    RTreeUtils.enclose(n);

    if (nn != null) {
      RTreeUtils.enclose(nn);

      if (splitStrategy.needToSplit(n.getParent())) {
        Node[] groups = splitStrategy.split(n.getParent());
        adjustTree(groups[0], groups[1]);
      }
    }

    if (n.getParent() != null) {
      adjustTree(n.getParent(), null);
    }
  }

  public static <T> void print(RTree<T> tree)
  {
    Node<T> root = tree.getRoot();
    try {
    System.out.println(root.print(0));
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }
}
