/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import com.google.common.collect.Lists;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.traverse.AbstractGraphIterator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Graph iterator that supports weighted paths
 *
 * @author Mulesoft Inc.
 * @since 1.4.0
 */
public class WeightedPathIterator extends AbstractGraphIterator<FlowComponent, DefaultWeightedEdge> {

  private Queue<FlowComponent> toVisit;
  private List<DefaultWeightedEdge> visitedEdges;
  FlowComponent start;

  public WeightedPathIterator(Graph g, FlowComponent start) {
    super(g);
    this.start = start;
    this.visitedEdges = Lists.newArrayList();
    this.toVisit = new LinkedList();
    this.toVisit.offer(start);
  }

  @Override
  public boolean hasNext() {
    return !toVisit.isEmpty();
  }

  @Override
  public FlowComponent next() {
    FlowComponent nextToVisitNode = toVisit.poll();

    // if same flow use weight, if different flows try to find originating source
    Set<DefaultWeightedEdge> outgoingComponentsEdges = graph.outgoingEdgesOf(nextToVisitNode);
    Map<String, List<DefaultWeightedEdge>> edgesByFlow =
        outgoingComponentsEdges.stream().collect(Collectors.groupingBy(e -> (graph.getEdgeTarget(e)).getParentFlow().getName()));
    List<DefaultWeightedEdge> consideredEdges = null;
    if (edgesByFlow.size() > 1) {
      consideredEdges = edgesByFlow.get(start.getParentFlow().getName());
    } else {
      consideredEdges = edgesByFlow.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }
    if (consideredEdges != null) {
      Map<Double, List<DefaultWeightedEdge>> edgesByWeight =
          consideredEdges.stream().collect(Collectors.groupingBy(e -> (graph.getEdgeWeight(e))));
      Double nextEdgeWeightToVisit = consideredEdges.stream()
          .sorted(Comparator.comparingDouble(e -> graph.getEdgeWeight(e)))
          .filter(e -> !visitedEdges.contains(e))
          .findFirst().map(e1 -> graph.getEdgeWeight(e1)).orElse(null);

      List<DefaultWeightedEdge> possibleNextEdges = edgesByWeight.get(nextEdgeWeightToVisit);

      if (possibleNextEdges != null) {
        possibleNextEdges.forEach(edge -> {
          toVisit.add(graph.getEdgeTarget(edge));
          if (edgesByWeight.keySet().size() > 1) {
            visitedEdges.add(edge);
          }
        });
      }
    }

    return nextToVisitNode;
  }
}

