package com.metamx.common.spatial.rtree;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

/**
 */
public class Point<T> extends Node<T>
{
  private final double[] coords;
  private final T entry;

  public Point(double[] coords, T entry)
  {
    super(coords, Arrays.copyOf(coords, coords.length), true);

    this.coords = coords;
    this.entry = entry;
  }

  public double[] getCoords()
  {
    return coords;
  }

  public T getEntry()
  {
    return entry;
  }

  @Override
  public void updateMinCoordinates(double[] minCoords)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void updateMaxCoordinates(double[] maxCoords)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addChild(Node<T> node)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addChildren(List<Node<T>> nodes)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Node<T>> getChildren()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clearChildren()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isLeaf()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public double getArea()
  {
    return 0;
  }

  @Override
  public boolean contains(Node other)
  {
    return false;
  }

  @Override
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
    builder.append("Point{\n");
    builder.append(tabs2);

    ObjectMapper jsonMapper = new ObjectMapper();

    builder.append(String.format("coords: %s, entry: %s", jsonMapper.writeValueAsString(coords), entry));
    builder.append("\n");
    builder.append(tabs);
    builder.append("}");

    return builder.toString();
  }
}
