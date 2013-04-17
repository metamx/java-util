package com.metamx.common.spatial.rtree.split;

import com.google.common.collect.Lists;
import com.metamx.common.spatial.rtree.Node;
import com.metamx.common.spatial.rtree.RTreeUtils;

import java.util.List;

/**
 */
public abstract class GutmanSplitStrategy implements SplitStrategy
{
  private final int minNumChildren;
  private final int maxNumChildren;

  protected GutmanSplitStrategy(int minNumChildren, int maxNumChildren)
  {
    this.minNumChildren = minNumChildren;
    this.maxNumChildren = maxNumChildren;
  }

  @Override
  public boolean needToSplit(Node node)
  {
    return (node.getChildren().size() > maxNumChildren);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Node[] split(Node node)
  {
    List<Node> children = Lists.newArrayList(node.getChildren());
    Node[] seeds = pickSeeds(children);

    node.clearChildren();
    node.addChild(seeds[0]);

    Node group1 = new Node(
        seeds[1].getMinCoordinates(),
        seeds[1].getMaxCoordinates(),
        Lists.newArrayList(seeds[1]),
        node.isLeaf(),
        node.getParent()
    );
    if (node.getParent() != null) {
      node.getParent().addChild(group1);
    }
    Node[] groups = new Node[]{
        node, group1
    };

    RTreeUtils.enclose(groups);

    while (!children.isEmpty()) {
      for (Node group : groups) {
        if (group.getChildren().size() + children.size() <= minNumChildren) {
          group.addChildren(children);
          return groups;
        }
      }

      Node nextToAssign = pickNext(children, groups);
      double group0ExpandedArea = RTreeUtils.getEnclosingArea(groups[0], nextToAssign);
      double group1ExpandedArea = RTreeUtils.getEnclosingArea(groups[1], nextToAssign);

      Node optimal;
      if (group0ExpandedArea < group1ExpandedArea) {
        optimal = groups[0];
      } else if (group0ExpandedArea == group1ExpandedArea) {
        if (groups[0].getArea() < groups[1].getArea()) {
          optimal = groups[0];
        } else {
          optimal = groups[1];
        }
      } else {
        optimal = groups[1];
      }

      optimal.addChild(nextToAssign);
      optimal.enclose();
    }

    return groups;
  }

  public abstract Node[] pickSeeds(List<Node> nodes);

  public abstract Node pickNext(List<Node> nodes, Node[] groups);
}
