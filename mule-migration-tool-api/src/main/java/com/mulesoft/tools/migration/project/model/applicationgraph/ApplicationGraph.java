/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.jdom2.Element;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Mule application graph model
 *
 * @author Mulesoft Inc.
 * @since 1.3.0
 */
public class ApplicationGraph {

  Graph<FlowComponent, DefaultEdge> applicationGraph;

  public ApplicationGraph() {
    applicationGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
  }

  public void addConnectedFlowComponents(List<FlowComponent> flowComponents) {
    FlowComponent previousFlowComp = null;
    for (FlowComponent comp : flowComponents) {
      applicationGraph.addVertex(comp);
      if (previousFlowComp != null) {
        applicationGraph.addEdge(previousFlowComp, comp);
      }
      previousFlowComp = comp;
    }
  }

  public FlowComponent getStartingFlowComponent(Flow flow) {
    FlowComponent destinationMessageProcessor = getOneComponentOfFlow(flow);
    Set<DefaultEdge> incomingEdges = applicationGraph.incomingEdgesOf(destinationMessageProcessor);
    while (!incomingEdges.isEmpty()) {
      DefaultEdge singleIncomingEdge = Iterables.getOnlyElement(incomingEdges);
      destinationMessageProcessor = applicationGraph.getEdgeSource(singleIncomingEdge);
      incomingEdges = applicationGraph.incomingEdgesOf(destinationMessageProcessor);
    }

    return destinationMessageProcessor;
  }

  public FlowComponent getLastFlowComponent(Flow flow) {
    FlowComponent nextMessageProcessor = getOneComponentOfFlow(flow);
    Set<DefaultEdge> outgoingEdges = getOutgoingEdgesInFlow(nextMessageProcessor, flow);
    while (!outgoingEdges.isEmpty()) {
      DefaultEdge singleOutgoingEdge = Iterables.getOnlyElement(outgoingEdges);
      nextMessageProcessor = applicationGraph.getEdgeTarget(singleOutgoingEdge);
      outgoingEdges = getOutgoingEdgesInFlow(nextMessageProcessor, flow);
    }

    return nextMessageProcessor;
  }

  private Set<DefaultEdge> getOutgoingEdgesInFlow(FlowComponent nextMessageProcessor, Flow flow) {
    return applicationGraph.outgoingEdgesOf(nextMessageProcessor).stream()
        .filter(e -> applicationGraph.getEdgeTarget(e).getParentFlow().equals(flow))
        .collect(Collectors.toSet());
  }

  public <T> List<T> getAllFlowComponents(Class<T> typeOfComponent) {
    return this.applicationGraph.vertexSet().stream()
        .filter(c -> typeOfComponent.isInstance(c))
        .map(typeOfComponent::cast)
        .collect(Collectors.toList());
  }

  public List<FlowComponent> getAllFlowComponents() {
    return this.applicationGraph.vertexSet().stream()
        .collect(Collectors.toList());
  }

  private FlowComponent getOneComponentOfFlow(Flow flow) {
    return this.applicationGraph.vertexSet().stream()
        .filter(flowComponent -> flowComponent.getParentFlow() == flow)
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Cannot find referenced flow in current application"));
  }


  public void addEdge(FlowComponent source, FlowComponent destination) {
    this.applicationGraph.addEdge(source, destination);
  }

  public Optional<PropertiesSource> findClosestPropertiesSource(FlowComponent flowComponent) {
    Queue<FlowComponent> incomingFlowComponents = new LinkedList<>();
    incomingFlowComponents.offer(flowComponent);
    FlowComponent currentFlowComponent = null;
    while (!incomingFlowComponents.isEmpty()) {
      currentFlowComponent = incomingFlowComponents.poll();
      if (currentFlowComponent instanceof PropertiesSource) {
        return Optional.of((PropertiesSource) currentFlowComponent);
      } else {
        applicationGraph.incomingEdgesOf(currentFlowComponent).stream()
            .map(e -> applicationGraph.getEdgeSource(e))
            .forEach(incomingFlowComponents::offer);
      }
    }

    return Optional.empty();
  }

  public List<FlowComponent> getAllStartingFlowComponents() {
    return this.applicationGraph.vertexSet().stream()
        .filter(v -> this.applicationGraph.inDegreeOf(v) == 0)
        .collect(Collectors.toList());
  }

  public FlowComponent findFlowComponent(Element element) {
    return this.applicationGraph.vertexSet().stream()
        .filter(v -> v.getXmlElement().equals(element))
        .findFirst().orElse(null);
  }

  public FlowComponent getNextComponent(FlowRef flowRef, Flow flow) {
    return applicationGraph.outgoingEdgesOf(flowRef)
        .stream()
        .map(e -> applicationGraph.getEdgeTarget(e))
        .filter(flowComponent -> flowComponent.getParentFlow().equals(flow))
        .findFirst()
        .orElse(null);
  }

  public <T> List<FlowComponent> getAllFlowComponentsOfTypeAlongPath(FlowComponent startingPoint, Class<T> componentType,
                                                                     String componentName) {
    Iterator<FlowComponent> iterator = new DepthFirstIterator<>(applicationGraph, startingPoint);
    List<FlowComponent> resultingFlowComponents = Lists.newArrayList();
    while (iterator.hasNext()) {
      FlowComponent flowComponent = iterator.next();
      if (componentType.isInstance(flowComponent) && flowComponent.getName().matches(componentName)) {
        resultingFlowComponents.add(flowComponent);
      }
    }
    return resultingFlowComponents;
  }
}
