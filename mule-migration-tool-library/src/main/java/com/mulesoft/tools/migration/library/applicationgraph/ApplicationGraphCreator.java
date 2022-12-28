/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.applicationgraph;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NS_URI;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getAllElementsFromNamespaceXpathSelector;

import com.mulesoft.tools.migration.library.nocompatibility.InboundToAttributesTranslator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.applicationgraph.ApplicationGraph;
import com.mulesoft.tools.migration.project.model.applicationgraph.BasicFlowComponent;
import com.mulesoft.tools.migration.project.model.applicationgraph.Flow;
import com.mulesoft.tools.migration.project.model.applicationgraph.FlowComponent;
import com.mulesoft.tools.migration.project.model.applicationgraph.FlowRefFlowComponent;
import com.mulesoft.tools.migration.project.model.applicationgraph.InvalidGraphStateException;
import com.mulesoft.tools.migration.project.model.applicationgraph.MessageSourceFlowComponent;
import com.mulesoft.tools.migration.project.model.applicationgraph.OperationSourceFlowComponent;
import com.mulesoft.tools.migration.project.model.applicationgraph.PropertyTranslator;
import com.mulesoft.tools.migration.project.model.applicationgraph.SourceType;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.util.XmlDslUtils;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GraphV2
 *
 * @author Mulesoft Inc.
 */
public class ApplicationGraphCreator {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationGraphCreator.class);

  private static final String FLOW_XPATH =
      getAllElementsFromNamespaceXpathSelector(CORE_NS_URI, ImmutableList.of("flow", "sub-flow"), true, false);

  public static final String MESSAGE_SOURCE_FILTER_EXPRESSION =
      getAllElementsFromNamespaceXpathSelector(InboundToAttributesTranslator.getSupportedConnectors().stream()
          .collect(Collectors.groupingBy(
                                         SourceType::getNamespaceUri,
                                         Collectors.mapping(SourceType::getType, Collectors.toList()))), false, true);

  public ApplicationGraph create(ApplicationModel applicationModel, MigrationReport report) {
    List<Flow> applicationFlows = applicationModel.getApplicationDocuments().values().stream()
        .map(this::getFlows)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());

    ApplicationGraph applicationGraph = new ApplicationGraph(getPropertyTranslator(applicationModel), applicationFlows);

    applicationFlows.forEach(flow -> {
      List<FlowComponent> flowComponents = getFlowComponents(flow, applicationGraph);
      flow.setComponents(flowComponents);
      applicationGraph.linealFlowWiring(flow);
    });

    try {
      applicationGraph.muleFlowWiring();
    } catch (InvalidGraphStateException e) {
      LOGGER.warn("Could not generate application graph: " + e.getMessage());
    }
    return applicationGraph;
  }

  private List<FlowComponent> getFlowComponents(Flow flow, ApplicationGraph applicationGraph) {
    Element flowAsXmL = flow.getXmlElement();

    return flowAsXmL.getContent().stream()
        .filter(Element.class::isInstance)
        .map(Element.class::cast)
        .map(xmlElement -> createFlowComponent(xmlElement, flow, applicationGraph))
        .collect(Collectors.toList());
  }

  private FlowComponent createFlowComponent(Element xmlElement, Flow parentFlow, ApplicationGraph applicationGraph) {
    if (isMessageSource(xmlElement, parentFlow)) {
      return new MessageSourceFlowComponent(xmlElement, parentFlow, applicationGraph,
                                            PropertiesSourceType.getRegistered(xmlElement.getNamespaceURI(),
                                                                               xmlElement.getName()));
    }
    if (isOperationSource(xmlElement, parentFlow)) {
      return new OperationSourceFlowComponent(xmlElement, parentFlow, applicationGraph,
                                              PropertiesSourceType.getRegistered(xmlElement.getNamespaceURI(),
                                                                                 xmlElement.getName()));
    }
    if ("flow-ref".equals(xmlElement.getName())) {
      return new FlowRefFlowComponent(xmlElement, parentFlow, applicationGraph);
    }

    return new BasicFlowComponent(xmlElement, parentFlow, applicationGraph);
  }

  private boolean isMessageSource(Element element, Flow flow) {
    return isPropertySource(element, flow) && element.getName().matches("(listener|inbound|polling).*");
  }

  private boolean isOperationSource(Element element, Flow flow) {
    return isPropertySource(element, flow) && !isMessageSource(element, flow);
  }

  private boolean isPropertySource(Element element, Flow flow) {
    List<Element> propertySources =
        XmlDslUtils.getChildrenMatchingExpression(flow.getXmlElement(), MESSAGE_SOURCE_FILTER_EXPRESSION, Filters.element());

    return propertySources.contains(element);
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

  private PropertyTranslator getPropertyTranslator(ApplicationModel applicationModel) {
    PropertyTranslator translator = new InboundToAttributesTranslator();
    translator.initializeTranslationsForApplicationSourceTypes(applicationModel);
    return translator;
  }
}
