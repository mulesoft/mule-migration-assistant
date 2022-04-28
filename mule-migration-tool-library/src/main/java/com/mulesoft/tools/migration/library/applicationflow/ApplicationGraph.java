package com.mulesoft.tools.migration.library.applicationflow;

import com.google.common.collect.Iterables;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.stream.Collectors;

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
    FlowComponent destinationMessageProcessor = firstComponentOfFlow(flow);
    Set<DefaultEdge> incomingEdges = applicationGraph.incomingEdgesOf(destinationMessageProcessor);
    while (!incomingEdges.isEmpty()) {
      DefaultEdge singleIncomingEdge = Iterables.getOnlyElement(incomingEdges);
      destinationMessageProcessor = applicationGraph.getEdgeSource(singleIncomingEdge);
      incomingEdges = applicationGraph.incomingEdgesOf(destinationMessageProcessor);
    }

    return destinationMessageProcessor;
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

  private FlowComponent firstComponentOfFlow(Flow flow) {
    return this.applicationGraph.vertexSet().stream()
        .filter(flowComponent -> flowComponent.getParentFlow() == flow)
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Cannot find referenced flow in current application"));
  }


  public void addEdge(FlowRef source, FlowComponent destination) {
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
}
