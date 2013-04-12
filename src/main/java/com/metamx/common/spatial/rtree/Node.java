package com.metamx.common.spatial.rtree;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

/**
 */
public class Node<T>
{
  private final double[] minCoordinates;
  private final double[] maxCoordinates;

  private final List<Node<T>> children;
  private final boolean isLeaf;

  private Node<T> parent;

  public Node(double[] minCoordinates, double[] maxCoordinates, boolean isLeaf)
  {
    this(
        minCoordinates,
        maxCoordinates,
        Lists.<Node<T>>newArrayList(),
        isLeaf,
        null
    );
  }

  public Node(
      double[] minCoordinates,
      double[] maxCoordinates,
      List<Node<T>> children,
      boolean isLeaf,
      Node<T> parent
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

  public double[] getMinCoordinates()
  {
    return minCoordinates;
  }

  public void updateMinCoordinates(double[] minCoords)
  {
    Preconditions.checkArgument(minCoords.length == minCoordinates.length);
    System.arraycopy(minCoords, 0, minCoordinates, 0, minCoordinates.length);
  }

  public double[] getMaxCoordinates()
  {
    return maxCoordinates;
  }

  public void updateMaxCoordinates(double[] maxCoords)
  {
    Preconditions.checkArgument(maxCoords.length == maxCoordinates.length);
    System.arraycopy(maxCoords, 0, maxCoordinates, 0, maxCoordinates.length);
  }

  public void setParent(Node<T> p)
  {
    parent = p;
  }

  public Node<T> getParent()
  {
    return parent;
  }

  public void addChild(Node<T> node)
  {
    children.add(node);
  }

  public void addChildren(List<Node<T>> nodes)
  {
    children.addAll(nodes);
  }

  public List<Node<T>> getChildren()
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

  public boolean contains(double[] coords)
  {
    Preconditions.checkArgument(getNumDims() == coords.length);

    for (int i = 0; i < getNumDims(); i++) {
      if (coords[i] < minCoordinates[i] || coords[i] > maxCoordinates[i]) {
        return false;
      }
    }
    return true;
  }

  private double calculateArea()
  {
    double area = 1.0;
    for (int i = 0; i < minCoordinates.length; i++) {
      area *= (maxCoordinates[i] - minCoordinates[i]);
    }
    return area;
  }

  public String print(int numTabs) throws Exception
  {
    String tabs = "";
    for (int i = 0; i < numTabs; i++) {
      tabs += "\t";
    }
    String tabs2 = tabs + "\t";

    StringBuilder builder = new StringBuilder();
    builder.append("\n");
    builder.append(tabs);
    builder.append("Node{\n");
    builder.append(tabs2);

    ObjectMapper jsonMapper = new ObjectMapper();

    builder.append("minCoordinates: ");
    builder.append(jsonMapper.writeValueAsString(minCoordinates));
    builder.append(", maxCoordinates: ");
    builder.append(jsonMapper.writeValueAsString(maxCoordinates));
    builder.append(String.format(", area: %s, isLeaf: %s\n", getArea(), isLeaf));
    if (getChildren().size() > 0) {
      builder.append(tabs2);
      builder.append("children: ");
      for (Node<T> tNode : getChildren()) {
        builder.append(tNode.print(numTabs + 1));
      }
    }
    builder.append("\n");
    builder.append(tabs);
    builder.append("}\n");

    return builder.toString();
  }
}
