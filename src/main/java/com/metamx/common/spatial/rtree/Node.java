package com.metamx.common.spatial.rtree;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

/**
 */
public class Node
{
  private final float[] minCoordinates;
  private final float[] maxCoordinates;

  private final List<Node> children;
  private final boolean isLeaf;

  private Node parent;

  public Node(float[] minCoordinates, float[] maxCoordinates, boolean isLeaf)
  {
    this(
        minCoordinates,
        maxCoordinates,
        Lists.<Node>newArrayList(),
        isLeaf,
        null
    );
  }

  public Node(
      float[] minCoordinates,
      float[] maxCoordinates,
      List<Node> children,
      boolean isLeaf,
      Node parent
  )
  {
    Preconditions.checkArgument(minCoordinates.length == maxCoordinates.length);

    this.minCoordinates = minCoordinates;
    this.maxCoordinates = maxCoordinates;
    this.children = children;
    this.isLeaf = isLeaf;
    this.parent = parent;
  }

  public int getNumDims()
  {
    return minCoordinates.length;
  }

  public float[] getMinCoordinates()
  {
    return minCoordinates;
  }

  public float[] getMaxCoordinates()
  {
    return maxCoordinates;
  }

  public void setParent(Node p)
  {
    parent = p;
  }

  public Node getParent()
  {
    return parent;
  }

  public void addChild(Node node)
  {
    node.setParent(this);
    children.add(node);
  }

  public void addChildren(List<Node> nodes)
  {
    for (Node node : nodes) {
      node.setParent(this);
    }
    children.addAll(nodes);
  }

  public List<Node> getChildren()
  {
    return children;
  }

  public void clearChildren()
  {
    children.clear();
  }

  public boolean isLeaf()
  {
    return isLeaf;
  }

  public double getArea()
  {
    return calculateArea();
  }

  public boolean contains(Node other)
  {
    Preconditions.checkArgument(getNumDims() == other.getNumDims());

    for (int i = 0; i < getNumDims(); i++) {
      if (other.getMinCoordinates()[i] < minCoordinates[i] || other.getMaxCoordinates()[i] > maxCoordinates[i]) {
        return false;
      }
    }
    return true;
  }

  public boolean contains(float[] coords)
  {
    Preconditions.checkArgument(getNumDims() == coords.length);

    for (int i = 0; i < getNumDims(); i++) {
      if (coords[i] < minCoordinates[i] || coords[i] > maxCoordinates[i]) {
        return false;
      }
    }
    return true;
  }

  public boolean enclose()
  {
    boolean retVal = false;
    float[] minCoords = new float[getNumDims()];
    Arrays.fill(minCoords, Float.MAX_VALUE);
    float[] maxCoords = new float[getNumDims()];
    Arrays.fill(maxCoords, -Float.MAX_VALUE);

    for (Node child : getChildren()) {
      for (int i = 0; i < getNumDims(); i++) {
        minCoords[i] = Math.min(child.getMinCoordinates()[i], minCoords[i]);
        maxCoords[i] = Math.max(child.getMaxCoordinates()[i], maxCoords[i]);
      }
    }

    if (!Arrays.equals(minCoords, minCoordinates)) {
      System.arraycopy(minCoords, 0, minCoordinates, 0, minCoordinates.length);
      retVal = true;
    }
    if (!Arrays.equals(maxCoords, maxCoordinates)) {
      System.arraycopy(maxCoords, 0, maxCoordinates, 0, maxCoordinates.length);
      retVal = true;
    }

    return retVal;
  }

  private double calculateArea()
  {
    double area = 1.0;
    for (int i = 0; i < minCoordinates.length; i++) {
      area *= (maxCoordinates[i] - minCoordinates[i]);
    }
    return area;
  }
}
