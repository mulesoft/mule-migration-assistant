/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
  private FlowComponent start;
  private Set<String> visitedFlows;

  public WeightedPathIterator(Graph g, FlowComponent start) {
    super(g);
    this.start = start;
    this.visitedEdges = Lists.newArrayList();
    this.toVisit = new LinkedList();
    this.toVisit.offer(start);
    this.visitedFlows = Sets.newHashSet();
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

    // in case there are multiple outgoing edges we need to consider only those that are related to the path that we are iterating through
    if (edgesByFlow.keySet().size() > 1) {
      consideredEdges = edgesByFlow.entrySet().stream().filter(e -> visitedFlows.contains(e.getKey())).map(
                                                                                                           Map.Entry::getValue)
          .flatMap(Collection::stream).collect(Collectors.toList());
    } else {
      if (edgesByFlow.keySet().size() > 0) {
        visitedFlows.add(edgesByFlow.keySet().iterator().next());
      }
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

  public Set<String> getVisitedFlows() {
    return visitedFlows;
  }
}

