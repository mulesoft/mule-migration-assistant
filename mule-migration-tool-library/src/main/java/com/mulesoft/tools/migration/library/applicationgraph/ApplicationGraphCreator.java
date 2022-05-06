/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.applicationgraph;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mulesoft.tools.migration.library.mule.steps.nocompatibility.InboundToAttributesTranslator;
import com.mulesoft.tools.migration.project.model.applicationgraph.*;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.apache.commons.lang3.tuple.Pair;
import org.jdom2.*;
import org.jdom2.filter.Filter;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathFactory;

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

  public ApplicationGraph create(List<Document> applicationDocuments, MigrationReport report) throws RuntimeException {
    List<Flow> applicationFlows = applicationDocuments.stream()
        .map(this::getFlows)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

    ApplicationGraph applicationGraph = new ApplicationGraph();

    applicationFlows.forEach(flow -> {
      List<FlowComponent> flowComponents = getFlowComponents(flow, applicationFlows, report);
      flow.setComponents(flowComponents);
      applicationGraph.addConnectedFlowComponents(flowComponents);
    });

    // build ApplicationGraph based on flow components and flowRefs. 
    // This graph can have non connected components, if we have multiple flows with sources
    // get explicit connections (flow-refs)
    Map<FlowRef, Pair<FlowComponent, FlowComponent>> connectedFlows = getFlowRefMap(applicationGraph);
    connectedFlows.entrySet().forEach(connectedFlow -> {
      FlowRef flowRefComponent = connectedFlow.getKey();
      FlowComponent firstComponentOfReferencedFlow = connectedFlow.getValue().getLeft();
      FlowComponent endComponentOfReferencedFlow = applicationGraph.getLastFlowComponent(flowRefComponent.getDestinationFlow());
      FlowComponent originalFlowContinuation = connectedFlow.getValue().getRight();
      applicationGraph.addEdge(flowRefComponent, firstComponentOfReferencedFlow);
      if (originalFlowContinuation != null) {
        applicationGraph.addEdge(endComponentOfReferencedFlow, originalFlowContinuation);
      }
    });

    // TODO: add implicit connections (i.e: operations)
    // Is this actually needed? If we have an operation that calls a flow inside the same app, then handling
    // any property references should be the same as migrating that flow individually
    return applicationGraph;
  }

  private List<FlowComponent> getFlowComponents(Flow flow, List<Flow> applicationFlows, MigrationReport report) {
    Element flowAsXmL = flow.getXmlElement();

    return flowAsXmL.getContent().stream()
        .filter(Element.class::isInstance)
        .map(Element.class::cast)
        .map(xmlElement -> convertToComponent(xmlElement, flow, applicationFlows, report))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private FlowComponent convertToComponent(Element xmlElement, Flow parentFlow,
                                           List<Flow> applicationFlows, MigrationReport report) {
    if (xmlElement.getName().equals("flow-ref")) {
      if (!expressionMigrator.isWrapped(xmlElement.getAttribute("name").getValue())) {
        String destinationFlowName = xmlElement.getAttribute("name").getValue();
        Optional<Flow> destinationFlow = applicationFlows.stream()
            .filter(flow -> flow.getName().equals(destinationFlowName))
            .findFirst();

        if (destinationFlow.isPresent()) {
          return new FlowRef(xmlElement, parentFlow, destinationFlow.get());
        } else {
          report.report("nocompatibility.missingflow", xmlElement, xmlElement);
        }
      } else {
        report.report("nocompatibility.dynamicflowref", xmlElement, xmlElement);
      }
    } else if (isPropertySource(xmlElement, parentFlow)) {
      return new PropertiesSourceComponent(xmlElement, parentFlow);
    } else {
      return new MessageProcessor(xmlElement, parentFlow);
    }

    // we should never reach here
    return null;
  }

  private Map<FlowRef, Pair<FlowComponent, FlowComponent>> getFlowRefMap(ApplicationGraph applicationGraph) {
    Map<FlowRef, Pair<FlowComponent, FlowComponent>> connectedFlows = Maps.newHashMap();
    applicationGraph.getAllFlowComponents(FlowRef.class).forEach(flowRef -> {
      FlowComponent goBackFlowComponent = applicationGraph.getNextComponent(flowRef, flowRef.getParentFlow());
      FlowComponent destinationFlowComponent = applicationGraph.getStartingFlowComponent(flowRef.getDestinationFlow());
      connectedFlows.put(flowRef, Pair.of(destinationFlowComponent, goBackFlowComponent));
    });

    return connectedFlows;
  }

  private List<Flow> getFlows(Document document) {
    List<Element> flowsAsXml = getChildrenMatchingExpression(document.getRootElement(), FLOW_XPATH, Filters.element());

    return flowsAsXml.stream()
        .map(this::convertToFlow)
        .collect(Collectors.toList());
  }

  private Flow convertToFlow(Element flowAsXml) {
    return new Flow(flowAsXml);
  }

  private boolean isPropertySource(Element element, Flow flow) {
    List<Element> propertySources =
        getChildrenMatchingExpression(flow.getXmlElement(), MESSAGE_SOURCE_FILTER_EXPRESSION, Filters.element());

    return propertySources.contains(element);
  }

  private <T> List<T> getChildrenMatchingExpression(Element elementToEvaluate, String expression, Filter<T> filter) {
    return XPathFactory.instance().compile(expression, filter).evaluate(elementToEvaluate);
  }

  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }
}
