package com.metamx.common.spatial.rtree;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

/**
 */
public class Point extends Node
{
  private final float[] coords;
  private final int entry;

  public Point(float[] coords, int entry)
  {
    super(coords, Arrays.copyOf(coords, coords.length), true);

    this.coords = coords;
    this.entry = entry;
  }

  public float[] getCoords()
  {
    return coords;
  }

  public int getEntry()
  {
    return entry;
  }

  @Override
  public void addChild(Node node)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addChildren(List<Node> nodes)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Node> getChildren()
  {
    return Lists.newArrayList();
  }

  @Override
  public void clearChildren()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isLeaf()
  {
    return true;
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
}
