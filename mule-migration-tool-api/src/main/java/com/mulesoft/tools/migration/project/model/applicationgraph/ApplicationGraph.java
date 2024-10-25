/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.MIGRATION_ID_ATTRIBUTE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.MIGRATION_NAMESPACE;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jdom2.Element;

/**
 * Mule application graph model
 *
 * @author Mulesoft Inc.
 */
public class ApplicationGraph {

  private final PropertyTranslator inboundTranslator;

  private final Map<String, FlowComponent> flowComponentIds = new HashMap<>();
  private final List<MessageSourceFlowComponent> sources = new ArrayList<>();
  private List<Flow> applicationFlows;

  public ApplicationGraph(PropertyTranslator translator, List<Flow> applicationFlows) {
    this.inboundTranslator = translator;
    this.applicationFlows = applicationFlows;
  }

  public FlowComponent findFlowComponent(Element element) {
    String elementId = element.getAttributeValue(MIGRATION_ID_ATTRIBUTE, MIGRATION_NAMESPACE);
    FlowComponent flowComponent = findFlowComponent(elementId);
    while (flowComponent == null && element.getParentElement() != null && !element.getParentElement().getName().equals("flow")) {
      element = element.getParentElement();
      elementId = element.getAttributeValue(MIGRATION_ID_ATTRIBUTE, MIGRATION_NAMESPACE);
      flowComponent = findFlowComponent(elementId);
    }

    return flowComponent;
  }

  private FlowComponent findFlowComponent(String elementId) {
    return flowComponentIds.get(elementId);
  }

  public void linealFlowWiring(Flow flow) {
    List<FlowComponent> flowComponents = (List<FlowComponent>) flow.getComponents();
    if (flowComponents.isEmpty())
      return;
    FlowComponent firstFlowComponent = flowComponents.get(0);
    if (firstFlowComponent instanceof MessageSourceFlowComponent) {
      sources.add((MessageSourceFlowComponent) firstFlowComponent);
      flowComponents.add(DummyFlowTerminalComponent.build(flow, this));
    } else if (flowComponents.get(flowComponents.size() - 1) instanceof FlowRefFlowComponent) {
      flowComponents.add(DummyFlowRefReturnComponent.build(flow, this));
    }
    FlowComponent previousFlowComp = null;
    for (FlowComponent comp : flowComponents) {
      flowComponentIds.put(comp.getElementId(), comp);
      if (previousFlowComp != null) {
        previousFlowComp.next(comp);
      }
      previousFlowComp = comp;
    }
  }

  public void muleFlowWiring() {
    Deque<Flow> flowStack = new ArrayDeque<>();
    for (MessageSourceFlowComponent source : sources) {
      FlowComponent current = source;
      flowStack.clear();
      while (!(current instanceof DummyFlowTerminalComponent)) {
        current = current.rewire(flowStack);
      }
      source.setTerminalComponent(current);
      flowComponentIds.put(current.getElementId(), current);
    }
  }

  public PropertyTranslator getInboundTranslator() {
    return inboundTranslator;
  }

  public Map<String, FlowComponent> getFlowComponentIds() {
    return new HashMap<>(flowComponentIds);
  }

  public Optional<Flow> getFlow(String flowName) {
    return applicationFlows.stream().filter(f -> f.getName().equals(flowName)).findFirst();
  }
}
