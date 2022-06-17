/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import com.google.common.collect.Iterables;
import org.jdom2.Element;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.*;
import java.util.stream.Collectors;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.MIGRATION_NAMESPACE;

/**
 * Mule application graph model
 *
 * @author Mulesoft Inc.
 * @since 1.3.0
 */
public class ApplicationGraph {

  DefaultDirectedGraph<FlowComponent, DefaultWeightedEdge> applicationGraph;
  PropertyTranslator inboundTranslator;

  public ApplicationGraph(PropertyTranslator inboundTranslator) {
    applicationGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    this.inboundTranslator = inboundTranslator;
  }
  
  public ApplicationGraph(ApplicationGraph parentGraph, FlowComponent startingPoint) {
    this(parentGraph.getInboundTranslator());
    WeightedPathIterator iterator = new WeightedPathIterator(parentGraph.applicationGraph, startingPoint);
    while (iterator.hasNext()) {
      FlowComponent currentNode = iterator.next();
      this.applicationGraph.addVertex(currentNode);
      parentGraph.getAllIncomingNodes(currentNode)
          .stream()
          .filter(this.applicationGraph::containsVertex)
          .forEach(v -> this.addEdge(v, currentNode));
    }
  }

  // Graph Access

  /**
   * Retrieves the first flow component in a Flow (the one that has no incoming edges)
   * @param flow
   * @return
   */
  public FlowComponent getStartingFlowComponent(Flow flow) {
    FlowComponent destinationMessageProcessor = getOneComponentOfFlow(flow);
    Set<DefaultWeightedEdge> incomingEdges = applicationGraph.incomingEdgesOf(destinationMessageProcessor).stream()
        .filter(edge -> applicationGraph.getEdgeSource(edge).getParentFlow().equals(flow)).collect(Collectors.toSet());
    while (!incomingEdges.isEmpty()) {
      DefaultWeightedEdge singleIncomingEdge = Iterables.getOnlyElement(incomingEdges);
      destinationMessageProcessor = applicationGraph.getEdgeSource(singleIncomingEdge);
      incomingEdges = applicationGraph.incomingEdgesOf(destinationMessageProcessor);
    }

    return destinationMessageProcessor;
  }

  /**
   * Retrieves the last flow component in a flow (the one that has no outgoing edges)
   * @param flow
   * @return
   */
  public FlowComponent getLastFlowComponent(Flow flow) {
    FlowComponent nextMessageProcessor = getOneComponentOfFlow(flow);
    Set<DefaultWeightedEdge> outgoingEdges = getOutgoingEdgesInFlow(nextMessageProcessor, flow);
    while (!outgoingEdges.isEmpty()) {
      DefaultWeightedEdge singleOutgoingEdge = Iterables.getOnlyElement(outgoingEdges);
      nextMessageProcessor = applicationGraph.getEdgeTarget(singleOutgoingEdge);
      outgoingEdges = getOutgoingEdgesInFlow(nextMessageProcessor, flow);
    }

    return nextMessageProcessor;
  }

  /**
  * Retrieves all the node from one type
  * @param typeOfComponent
  * @param <T>
  * @return
  */
  public <T> List<T> getAllFlowComponents(Class<T> typeOfComponent) {
    return this.applicationGraph.vertexSet().stream()
        .filter(c -> typeOfComponent.isInstance(c))
        .map(typeOfComponent::cast)
        .collect(Collectors.toList());
  }

  /**
   * Retrieves all starting points of the full application
   * @return
   */
  public List<FlowComponent> getAllStartingFlowComponents() {
    return this.applicationGraph.vertexSet().stream()
        .filter(v -> this.applicationGraph.inDegreeOf(v) == 0)
        .collect(Collectors.toList());
  }

  /**
   * Retrieves a flow component from an XML element
   * @param element
   * @return
   */
  public FlowComponent findFlowComponent(Element element) {
    String elementId = element.getAttributeValue("migrationId", MIGRATION_NAMESPACE);
    FlowComponent flowComponent = findFlowComponent(elementId);
    while (flowComponent == null && element.getParentElement() != null && !element.getParentElement().getName().equals("flow")) {
      element = element.getParentElement();
      elementId = element.getAttributeValue("migrationId", MIGRATION_NAMESPACE);
      flowComponent = findFlowComponent(elementId);
    }

    return flowComponent;
  }

  /**
   * Given a flow component it will get the next component in a flow (after the next flow is executed)
   * @param flowRef
   * @param flow
   * @return
   */
  public FlowComponent getNextComponent(FlowComponent flowRef, Flow flow) {
    return applicationGraph.outgoingEdgesOf(flowRef)
        .stream()
        .map(e -> applicationGraph.getEdgeTarget(e))
        .filter(flowComponent -> flowComponent.getParentFlow().equals(flow))
        .findFirst()
        .orElse(null);
  }

  /**
   * Finds all existing flow components with the same base name
   * @param prefix
   * @return
   */
  public List<String> getAllVertexNamesWithBaseName(String prefix) {
    return this.applicationGraph.vertexSet().stream()
        .map(FlowComponent::getName)
        .filter(name -> name.equals(prefix) || name.startsWith(prefix + "-"))
        .collect(Collectors.toList());
  }

  /**
   * Get all flow components that have incoming edges to a certain component
   * @param component
   * @return
   */
  public List<FlowComponent> getAllIncomingNodes(FlowComponent component) {
    return applicationGraph.incomingEdgesOf(component).stream()
        .map(edge -> applicationGraph.getEdgeSource(edge))
        .collect(Collectors.toList());
  }

  public WeightedPathIterator getWeightedPathIterator(FlowComponent start) {
    return new WeightedPathIterator(this.applicationGraph, start);
  }

  /**
   * Determines if a component is a leaf (no outgoing edges)
   * @param component
   * @return
   */
  public boolean isLeafComponent(FlowComponent component) {
    return applicationGraph.outgoingEdgesOf(component).isEmpty();
  }

  public PropertyTranslator getInboundTranslator() {
    return this.inboundTranslator;
  }

  // Graph Manipulation

  public void addEdge(FlowComponent source, FlowComponent destination) {
    if (applicationGraph.getEdge(source, destination) == null) {
      Optional<DefaultWeightedEdge> lastAddedEdge = applicationGraph.outgoingEdgesOf(source).stream()
          .filter(e -> applicationGraph.getEdgeTarget(e).getParentFlow().equals(destination.getParentFlow()))
          .sorted(Comparator.comparingDouble(e -> applicationGraph.getEdgeWeight(e)))
          .findFirst();
      double weight = 1.0d;

      if (lastAddedEdge.isPresent()) {
        weight = applicationGraph.getEdgeWeight(lastAddedEdge.get()) + 1.0d;
      }

      this.addEdge(source, destination, weight);
    }
  }

  public void addFlowComponent(FlowComponent component) {
    this.applicationGraph.addVertex(component);
  }

  public void removeEdgeIfExists(FlowRef flowRefComponent, FlowComponent originalFlowContinuation) {
    if (flowRefComponent != null && originalFlowContinuation != null
        && applicationGraph.getEdge(flowRefComponent, originalFlowContinuation) != null) {
      this.applicationGraph.removeEdge(flowRefComponent, originalFlowContinuation);
    }
  }

  public void addConnections(List<FlowComponent> flowComponents) {
    FlowComponent previousFlowComp = null;
    for (FlowComponent comp : flowComponents) {
      applicationGraph.addVertex(comp);
      if (previousFlowComp != null) {
        applicationGraph.addEdge(previousFlowComp, comp);
      }
      previousFlowComp = comp;
    }
  }

  private Set<DefaultWeightedEdge> getOutgoingEdgesInFlow(FlowComponent nextMessageProcessor, Flow flow) {
    return applicationGraph.outgoingEdgesOf(nextMessageProcessor).stream()
        .filter(e -> applicationGraph.getEdgeTarget(e).getParentFlow().equals(flow))
        .collect(Collectors.toSet());
  }

  private FlowComponent getOneComponentOfFlow(Flow flow) {
    return this.applicationGraph.vertexSet().stream()
        .filter(flowComponent -> flowComponent.getParentFlow() == flow)
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Cannot find referenced flow in current application"));
  }

  private FlowComponent findFlowComponent(String elementId) {
    return this.applicationGraph.vertexSet().stream()
        .filter(fc -> matchesElementId(fc, elementId))
        .findFirst()
        .map(fc -> getComponent(fc, elementId))
        .orElse(null);
  }

  private FlowComponent getComponent(FlowComponent flowComponent, String elementId) {
    if (matchesIdInResponseComponent(flowComponent, elementId)) {
      return ((PropertiesSourceComponent) flowComponent).getResponseComponent();
    } else if (matchesIdInErrorResponseComponent(flowComponent, elementId)) {
      return ((PropertiesSourceComponent) flowComponent).getErrorResponseComponent();
    } else {
      return flowComponent;
    }
  }

  private boolean matchesElementId(FlowComponent flowComp, String elementId) {
    return flowComp.getElementId().equals(elementId) || matchesIdInResponseComponent(flowComp, elementId)
        || matchesIdInErrorResponseComponent(flowComp, elementId);
  }

  private boolean matchesIdInResponseComponent(FlowComponent flowComp, String elementId) {
    if (PropertiesSourceComponent.class.isInstance(flowComp)) {
      MessageProcessor responseComponent = ((PropertiesSourceComponent) flowComp).getResponseComponent();
      return responseComponent != null && elementId.equals(responseComponent.getElementId());
    }
    return false;
  }

  private boolean matchesIdInErrorResponseComponent(FlowComponent flowComp, String elementId) {
    if (PropertiesSourceComponent.class.isInstance(flowComp)) {
      MessageProcessor exceptionResponseComponent = ((PropertiesSourceComponent) flowComp).getErrorResponseComponent();
      return exceptionResponseComponent != null && elementId.equals(exceptionResponseComponent.getElementId());
    }
    return false;
  }

  private void addEdge(FlowComponent source, FlowComponent destination, Double weight) {
    this.applicationGraph.addEdge(source, destination);
    this.applicationGraph.setEdgeWeight(source, destination, weight);
  }
}
