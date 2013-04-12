package com.metamx.common.spatial.rtree;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.List;

/**
 */
public class RTreeUtils
{
  public static double getEnclosingArea(Node a, Node b)
  {
    Preconditions.checkArgument(a.getNumDims() == b.getNumDims());

    double[] minCoords = new double[a.getNumDims()];
    double[] maxCoords = new double[a.getNumDims()];

    for (int i = 0; i < minCoords.length; i++) {
      minCoords[i] = Math.min(a.getMinCoordinates()[i], b.getMinCoordinates()[i]);
      maxCoords[i] = Math.max(a.getMaxCoordinates()[i], b.getMaxCoordinates()[i]);
    }

    double area = 1.0;
    for (int i = 0; i < minCoords.length; i++) {
      area *= (maxCoords[i] - minCoords[i]);
    }

    return area;
  }

  public static double getExpansionCost(Node node, Point point)
  {
    Preconditions.checkArgument(node.getNumDims() == point.getNumDims());

    if (node.contains(point.getCoords())) {
      return 0;
    }

    double expanded = 1.0;
    for (int i = 0; i < node.getNumDims(); i++) {
      double min = Math.min(point.getCoords()[i], node.getMinCoordinates()[i]);
      double max = Math.max(point.getCoords()[i], node.getMinCoordinates()[i]);
      expanded *= (max - min);
    }

    return (expanded - node.getArea());
  }

  public static void enclose(Node[] nodes)
  {
    for (Node node : nodes) {
      enclose(node);
    }
  }

  @SuppressWarnings("unchecked")
  public static void enclose(Node node)
  {
    double[] minCoords = new double[node.getNumDims()];
    Arrays.fill(minCoords, Double.MAX_VALUE);
    double[] maxCoords = new double[node.getNumDims()];
    Arrays.fill(maxCoords, Double.MIN_VALUE);

    for (Node child : (List<Node>) node.getChildren()) {
      for (int i = 0; i < node.getNumDims(); i++) {
        minCoords[i] = Math.min(child.getMinCoordinates()[i], minCoords[i]);
        maxCoords[i] = Math.max(child.getMaxCoordinates()[i], maxCoords[i]);
      }
    }

    node.updateMinCoordinates(minCoords);
    node.updateMaxCoordinates(maxCoords);
  }

  @SuppressWarnings("unchecked")
  public static Node copyOf(Node node)
  {
    return new Node(
        node.getMinCoordinates(),
        node.getMaxCoordinates(),
        node.getChildren(),
        node.isLeaf(),
        node.getParent()
    );
  }
}
