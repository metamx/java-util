package com.metamx.common.spatial.rtree.split;

import com.metamx.common.spatial.rtree.Node;

import java.util.List;

/**
 */
public class LinearGutmanSplitStrategy extends GutmanSplitStrategy
{
  public LinearGutmanSplitStrategy(int minNumChildren, int maxNumChildren)
  {
    super(minNumChildren, maxNumChildren);
  }

  @Override
  public Node[] pickSeeds(List<Node> nodes)
  {
    int[] optimalIndices = new int[2];
    int numDims = nodes.get(0).getNumDims();

    double bestNormalized = 0.0;
    for (int i = 0; i < numDims; i++) {
      double minCoord = Double.MAX_VALUE;
      double maxCoord = Double.MIN_VALUE;
      double highestLowSide = Double.MIN_VALUE;
      double lowestHighside = Double.MAX_VALUE;
      int highestLowSideIndex = 0;
      int lowestHighSideIndex = 0;

      int counter = 0;
      for (Node node : nodes) {
        minCoord = Math.min(minCoord, node.getMinCoordinates()[i]);
        maxCoord = Math.max(maxCoord, node.getMaxCoordinates()[i]);

        if (node.getMinCoordinates()[i] > highestLowSide) {
          highestLowSide = node.getMinCoordinates()[i];
          highestLowSideIndex = counter;
        }
        if (node.getMaxCoordinates()[i] < lowestHighside) {
          lowestHighside = node.getMaxCoordinates()[i];
          lowestHighSideIndex = counter;
        }

        counter++;
      }
      double normalizedSeparation = (highestLowSideIndex == lowestHighSideIndex) ? -1.0 :
                          Math.abs((highestLowSide - lowestHighside) / (maxCoord - minCoord));
      if (normalizedSeparation > bestNormalized) {
        optimalIndices[0] = highestLowSideIndex;
        optimalIndices[1] = lowestHighSideIndex;
        bestNormalized = normalizedSeparation;
      }
    }

    // Didn't actually find anything, just return first 2 children
    if (bestNormalized == 0) {
      optimalIndices[0] = 0;
      optimalIndices[1] = 1;
    }

    return new Node[]{nodes.remove(optimalIndices[0]), nodes.remove(optimalIndices[1] - 1)};
  }

  @Override
  public Node pickNext(List<Node> nodes, Node[] groups)
  {
    return nodes.remove(0);
  }
}
