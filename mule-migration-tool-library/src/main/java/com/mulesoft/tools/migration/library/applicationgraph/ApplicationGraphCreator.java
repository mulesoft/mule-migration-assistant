/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.applicationgraph;

import com.google.common.collect.ImmutableList;
import com.mulesoft.tools.migration.library.nocompatibility.InboundToAttributesTranslator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.applicationgraph.*;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.util.XmlDslUtils;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.apache.commons.lang3.tuple.Pair;
import org.jdom2.*;
import org.jdom2.filter.Filters;

import java.util.*;
import java.util.stream.Collectors;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.*;

/**
 * Step that creates an application graph and uses it to translate properties and variables into the mule 4 model
 *
 * @author Mulesoft Inc.
 * @since 1.3.0
 */
public class ApplicationGraphCreator {

  public static final String FLOW_XPATH =
      getAllElementsFromNamespaceXpathSelector(CORE_NS_URI, ImmutableList.of("flow", "sub-flow"), true, false);

  public static final String MESSAGE_SOURCE_FILTER_EXPRESSION =
      getAllElementsFromNamespaceXpathSelector(InboundToAttributesTranslator.getSupportedConnectors().stream()
          .collect(Collectors.groupingBy(
                                         SourceType::getNamespaceUri,
                                         Collectors.mapping(SourceType::getType, Collectors.toList()))), false, true);

  private ExpressionMigrator expressionMigrator;
  private ApplicationPropertiesContextCalculator applicationPropertiesContextCalculator;

  public ApplicationGraphCreator() {
    this.applicationPropertiesContextCalculator = new ApplicationPropertiesContextCalculator();
  }

  public ApplicationGraph create(ApplicationModel applicationModel, MigrationReport report) throws RuntimeException {
    List<Flow> applicationFlows = applicationModel.getApplicationDocuments().values().stream()
        .map(this::getFlows)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

    PropertyTranslator translator = new InboundToAttributesTranslator();
    translator.initializeTranslationsForApplicationSourceTypes(applicationModel);
    ApplicationGraph applicationGraph = new ApplicationGraph(translator);

    applicationFlows.forEach(flow -> {
      List<FlowComponent> flowComponents = getFirstLevelFlowComponents(flow, applicationFlows, report, applicationGraph);
      flow.setComponents(flowComponents);
      applicationGraph.addConnections(flowComponents);
    });

    // build ApplicationGraph based on flow components and flowRefs. 
    // This graph can have non connected components, if we have multiple flows with sources
    // get explicit connections (flow-refs)
    Map<Pair<FlowComponent, FlowComponent>, Pair<FlowComponent, FlowComponent>> connectedFlows = getFlowRefMap(applicationGraph);
    connectedFlows.entrySet().forEach(connectedFlow -> {
      Pair<FlowComponent, FlowComponent> sourceComponentPair = connectedFlow.getKey();
      Pair<FlowComponent, FlowComponent> destinationComponentPair = connectedFlow.getValue();

      FlowRef flowRefComponent = (FlowRef) sourceComponentPair.getLeft();
      FlowComponent firstComponentOfReferencedFlow = sourceComponentPair.getRight();
      FlowComponent endComponentOfReferencedFlow = destinationComponentPair.getLeft();
      FlowComponent originalFlowContinuation = destinationComponentPair.getRight();
      applicationGraph.addEdge(flowRefComponent, firstComponentOfReferencedFlow);
      applicationGraph.removeEdgeIfExists(flowRefComponent, originalFlowContinuation);
      if (originalFlowContinuation != null) {
        applicationGraph.addEdge(endComponentOfReferencedFlow, originalFlowContinuation);
      } else {
        SyntheticMessageProcessor syntethicOriginalFlowContinuation =
            new SyntheticMessageProcessor("endFlow" + flowRefComponent.getParentFlow().getName(), "",
                                          flowRefComponent.getParentFlow(), applicationGraph);
        applicationGraph.addFlowComponent(syntethicOriginalFlowContinuation);
        applicationGraph.addEdge(endComponentOfReferencedFlow, syntethicOriginalFlowContinuation);
      }
    });

    applicationPropertiesContextCalculator.calculatePropertiesContext(applicationGraph);
    return applicationGraph;
  }

  private List<FlowComponent> getFirstLevelFlowComponents(Flow flow, List<Flow> applicationFlows, MigrationReport report,
                                                          ApplicationGraph applicationGraph) {
    Element flowAsXmL = flow.getXmlElement();

    return flowAsXmL.getContent().stream()
        .filter(Element.class::isInstance)
        .map(Element.class::cast)
        .map(xmlElement -> convertAndAddToGraph(xmlElement, flow, applicationFlows, report, applicationGraph))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private FlowComponent convertAndAddToGraph(Element xmlElement, Flow parentFlow,
                                             List<Flow> applicationFlows, MigrationReport report,
                                             ApplicationGraph applicationGraph) {
    FlowComponent component = null;
    if (xmlElement.getName().equals("flow-ref")) {
      if (!expressionMigrator.isWrapped(xmlElement.getAttribute("name").getValue())) {
        String destinationFlowName = xmlElement.getAttribute("name").getValue();
        Optional<Flow> destinationFlow = applicationFlows.stream()
            .filter(flow -> flow.getName().equals(destinationFlowName))
            .findFirst();

        if (destinationFlow.isPresent()) {
          return new FlowRef(xmlElement, parentFlow, destinationFlow.get(), applicationGraph);
        } else {
          report.report("nocompatibility.missingflow", xmlElement, xmlElement);
        }
      } else {
        report.report("nocompatibility.dynamicflowref", xmlElement, xmlElement);
      }
    } else if (isPropertySource(xmlElement, parentFlow)) {
      component =
          new PropertiesSourceComponent(xmlElement,
                                        PropertiesSourceType.getRegistered(xmlElement.getNamespaceURI(), xmlElement.getName()),
                                        parentFlow, applicationGraph);
    } else {
      String componentName = String.format("%s:%s", xmlElement.getNamespace().getURI(), xmlElement.getName());
      switch (componentName) {
        case CORE_NS_URI + ":" + "set-property":
          component = new SetPropertyProcessor(xmlElement, parentFlow, applicationGraph);
          break;
        case CORE_NS_URI + ":" + "remove-property":
          component = new RemovePropertyProcessor(xmlElement, parentFlow, applicationGraph);
          break;
        case CORE_NS_URI + ":" + "copy-properties":
          component = new CopyPropertiesProcessor(xmlElement, parentFlow, applicationGraph);;
          break;
        default:
          component = new MessageProcessor(xmlElement, parentFlow, applicationGraph);
      }
    }

    applicationGraph.addFlowComponent(component);
    return component;
  }

  private Map<Pair<FlowComponent, FlowComponent>, Pair<FlowComponent, FlowComponent>> getFlowRefMap(ApplicationGraph applicationGraph) {
    return applicationGraph.getAllFlowComponents(FlowRef.class).stream()
        .map(flowRef -> getConnectedComponents(flowRef, applicationGraph))
        .collect(Collectors.toMap(connectedComponent -> connectedComponent.getLeft(),
                                  connectedComponent -> connectedComponent.getRight()));
  }

  private Pair<Pair<FlowComponent, FlowComponent>, Pair<FlowComponent, FlowComponent>> getConnectedComponents(FlowRef flowRef,
                                                                                                              ApplicationGraph applicationGraph) {
    FlowComponent referredFlowStartingPoint = applicationGraph.getStartingFlowComponent(flowRef.getDestinationFlow());
    if (referredFlowStartingPoint instanceof PropertiesSource
        && ((PropertiesSource) referredFlowStartingPoint).getType().isFlowSource()) {
      referredFlowStartingPoint =
          applicationGraph.getNextComponent(referredFlowStartingPoint, referredFlowStartingPoint.getParentFlow());
    }
    Pair<FlowComponent, FlowComponent> sourcePair = Pair.of(flowRef, referredFlowStartingPoint);

    FlowComponent goBackFlowComponent = applicationGraph.getNextComponent(flowRef, flowRef.getParentFlow());
    FlowComponent referredFlowEndingPoint = applicationGraph.getLastFlowComponent(flowRef.getDestinationFlow());
    if (referredFlowEndingPoint instanceof FlowRef) {
      Pair<Pair<FlowComponent, FlowComponent>, Pair<FlowComponent, FlowComponent>> newFlowRef =
          getConnectedComponents((FlowRef) referredFlowEndingPoint, applicationGraph);
      referredFlowEndingPoint = newFlowRef.getRight().getLeft();
    }

    Pair<FlowComponent, FlowComponent> destinationPair = Pair.of(referredFlowEndingPoint, goBackFlowComponent);
    return Pair.of(sourcePair, destinationPair);
  }

  private List<Flow> getFlows(Document document) {
    List<Element> flowsAsXml =
        XmlDslUtils.getChildrenMatchingExpression(document.getRootElement(), FLOW_XPATH, Filters.element());

    return flowsAsXml.stream()
        .map(this::convertToFlow)
        .collect(Collectors.toList());
  }

  private Flow convertToFlow(Element flowAsXml) {
    return new Flow(flowAsXml);
  }

  private boolean isPropertySource(Element element, Flow flow) {
    List<Element> propertySources =
        XmlDslUtils.getChildrenMatchingExpression(flow.getXmlElement(), MESSAGE_SOURCE_FILTER_EXPRESSION, Filters.element());

    return propertySources.contains(element);
  }

  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }
}
