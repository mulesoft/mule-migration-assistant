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

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.MIGRATION_NAMESPACE;

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

  private FlowComponent getOneComponentOfFlow(Flow flow) {
    return this.applicationGraph.vertexSet().stream()
        .filter(flowComponent -> flowComponent.getParentFlow() == flow)
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Cannot find referenced flow in current application"));
  }


  public void addEdge(FlowComponent source, FlowComponent destination) {
    this.applicationGraph.addEdge(source, destination);
  }

  public void addFlowComponent(FlowComponent component) {
    this.applicationGraph.addVertex(component);
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

  public FlowComponent findFlowComponent(String elementId) {
    return this.applicationGraph.vertexSet().stream()
        .filter(fc -> matchesElementId(fc, elementId))
        .findFirst()
        .map(fc -> getComponent(fc, elementId))
        .orElse(null);
  }

  private FlowComponent getComponent(FlowComponent flowComponent, String elementId) {
    if (matchesIdInResponseComponent(flowComponent, elementId)) {
      return ((PropertiesSourceComponent) flowComponent).getResponseComponent();
    } else {
      return flowComponent;
    }
  }

  private boolean matchesElementId(FlowComponent flowComp, String elementId) {
    return flowComp.getElementId().equals(elementId) || matchesIdInResponseComponent(flowComp, elementId);
  }

  private boolean matchesIdInResponseComponent(FlowComponent flowComp, String elementId) {
    if (PropertiesSourceComponent.class.isInstance(flowComp)) {
      FlowComponent responseComponent = ((PropertiesSourceComponent) flowComp).getResponseComponent();
      return responseComponent != null && elementId.equals(responseComponent.getElementId());
    }
    return false;
  }

  public FlowComponent findFlowComponent(Element element) {
    String elementId = element.getAttributeValue("migrationId", MIGRATION_NAMESPACE);
    FlowComponent flowComponent = findFlowComponent(elementId);
    while (flowComponent == null && element.getParentElement() != null && !element.getName().equals("flow")) {
      element = element.getParentElement();
      elementId = element.getAttributeValue("migrationId", MIGRATION_NAMESPACE);
      flowComponent = findFlowComponent(elementId);
    }

    return flowComponent;
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
      if (componentType.isInstance(flowComponent) && flowComponent.getName().startsWith(componentName)) {
        resultingFlowComponents.add(flowComponent);
      }
    }
    return resultingFlowComponents;
  }

  public void removeEdgeIfExists(FlowRef flowRefComponent, FlowComponent originalFlowContinuation) {
    if (applicationGraph.getEdge(flowRefComponent, originalFlowContinuation) != null) {
      this.applicationGraph.removeEdge(flowRefComponent, originalFlowContinuation);
    }
  }

  public List<String> getAllVertexNamesWithBaseName(String prefix) {
    return this.applicationGraph.vertexSet().stream()
        .map(FlowComponent::getName)
        .filter(name -> name.equals(prefix) || name.startsWith(prefix + "-"))
        .collect(Collectors.toList());
  }

  public DepthFirstIterator getDepthFirstIterator(FlowComponent start) {
    return new DepthFirstIterator(this.applicationGraph, start);
  }
}
