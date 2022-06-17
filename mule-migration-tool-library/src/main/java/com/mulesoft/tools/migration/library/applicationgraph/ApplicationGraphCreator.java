/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.applicationgraph;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.kitfox.svg.A;
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
import java.util.function.Function;
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
    PropertyTranslator translator = new InboundToAttributesTranslator();
    translator.initializeTranslationsForApplicationSourceTypes(applicationModel);
    ApplicationGraph applicationGraph = new ApplicationGraph(translator);
    List<Document> applicationDocuments = Lists.newArrayList(applicationModel.getApplicationDocuments().values());
    
    List<Flow> applicationFlows = applicationDocuments.stream()
        .map(d -> getElementsFromXml(d, FLOW_XPATH, this::convertToFlow))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

    applicationFlows.forEach(flow -> {
      List<FlowComponent> flowComponents = getFirstLevelComponents(flow, applicationFlows, report, applicationGraph);
      applicationGraph.addConnections(flowComponents);
    });

    // build ApplicationGraph based on flow components and flowRefs. 
    // This graph can have non connected components, if we have multiple flows with sources
    // get explicit connections (flow-refs)
    connectAllFlowRefs(applicationGraph);
    applicationFlows.forEach(flow -> {
      Element exceptionStrategyElement = getFlowExceptionHandlingElement(flow.getXmlElement());
      if (exceptionStrategyElement != null) {
        ExceptionStrategy flowExceptionStrategy = convertToExceptionStrategy(exceptionStrategyElement, flow, applicationFlows, report, applicationGraph);
        flow.setExceptionStrategy(flowExceptionStrategy);
      }
    });

    applicationPropertiesContextCalculator.calculatePropertiesContext(applicationGraph);
    return applicationGraph;
  }

  private List<FlowComponent> getFirstLevelComponents(Flow flow, List<Flow> applicationFlows, MigrationReport report,
                                                      ApplicationGraph applicationGraph) {
    return this.getFirstLevelComponents(flow.getXmlElement(), flow, applicationFlows, report, applicationGraph);
  }

  private List<FlowComponent> getFirstLevelComponents(Element xmlELement, Flow flow, List<Flow> applicationFlows, MigrationReport report,
                                                          ApplicationGraph applicationGraph) {
    return xmlELement.getContent().stream()
        .filter(Element.class::isInstance)
        .map(Element.class::cast)
        .map(subElement -> convertAndAddToGraph(subElement, flow, applicationFlows, report, applicationGraph))
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
          if (!isErrorHanldingElement(xmlElement)) {
            component = new MessageProcessor(xmlElement, parentFlow, applicationGraph);
          }
      }
    }

    applicationGraph.addFlowComponent(component);
    return component;
  }

  private void connectAllFlowRefs(ApplicationGraph graph) {
    graph.getAllFlowComponents(FlowRef.class).forEach(flowRef -> connectFlowRefComponents(flowRef, graph));

    // remove immediate connections between flow refs and components in the same flow
    graph.getAllFlowComponents(FlowRef.class).forEach(flowRef -> {
      FlowComponent flowComponent = graph.getNextComponent(flowRef, flowRef.getParentFlow());
      graph.removeEdgeIfExists(flowRef, flowComponent);
    });
  }

  private FlowComponent connectFlowRefComponents(FlowRef flowRef, ApplicationGraph applicationGraph) {
    FlowComponent referredFlowStartingPoint = applicationGraph.getStartingFlowComponent(flowRef.getDestinationFlow());
    if (referredFlowStartingPoint instanceof PropertiesSource
        && ((PropertiesSource) referredFlowStartingPoint).getType().isFlowSource()) {
      referredFlowStartingPoint =
          applicationGraph.getNextComponent(referredFlowStartingPoint, referredFlowStartingPoint.getParentFlow());
    }

    applicationGraph.addEdge(flowRef, referredFlowStartingPoint);

    FlowComponent referredFlowEndingPoint = applicationGraph.getLastFlowComponent(flowRef.getDestinationFlow());
    FlowComponent goBackFlowComponent = applicationGraph.getNextComponent(flowRef, flowRef.getParentFlow());
    if (referredFlowEndingPoint instanceof FlowRef) {

      referredFlowEndingPoint = connectFlowRefComponents((FlowRef) referredFlowEndingPoint, applicationGraph);
    }

    if (goBackFlowComponent == null) {
      goBackFlowComponent =
          new SyntheticMessageProcessor("endFlow" + flowRef.getParentFlow().getName(), "",
                                        flowRef.getParentFlow(), applicationGraph);
      applicationGraph.addFlowComponent(goBackFlowComponent);
      applicationGraph.addEdge(flowRef, goBackFlowComponent);
    }

    applicationGraph.addEdge(referredFlowEndingPoint, goBackFlowComponent);

    return referredFlowEndingPoint;
  }

  private <T> List<T> getElementsFromXml(Document document, String elementExpression, Function<Element, T> convertFunction) {
    List<Element> flowsAsXml =
        XmlDslUtils.getChildrenMatchingExpression(document.getRootElement(), FLOW_XPATH, Filters.element());

    return flowsAsXml.stream()
        .map(convertFunction::apply)
        .collect(Collectors.toList());
  }

  private Flow convertToFlow(Element flowAsXml) {
    return new Flow(flowAsXml);
  }
  
  private ExceptionStrategy convertToExceptionStrategy(Element xmlElement, Flow flow, List<Flow> applicationFlows, MigrationReport report, ApplicationGraph applicationGraph) {
    List<FlowComponent> scopedComponents = getFirstLevelComponents(xmlElement, flow, applicationFlows, report, applicationGraph);
    applicationGraph.addConnections(scopedComponents);
    ApplicationGraph subGraph = new ApplicationGraph(applicationGraph, scopedComponents.get(0));
    
    return new ExceptionStrategy(xmlElement, applicationGraph, subGraph);
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
