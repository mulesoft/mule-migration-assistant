/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import java.util.Deque;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Models a flow-ref component
 *
 * @author Mulesoft Inc.
 */
public class FlowRefFlowComponent extends BasicFlowComponent {

  private static final Logger LOGGER = LoggerFactory.getLogger(FlowRefFlowComponent.class);

  private final Flow targetFlow;
  private FlowComponent returnFlowComponent;
  protected boolean alreadyWired;

  public FlowRefFlowComponent(Element xmlElement, Flow parentFLow, ApplicationGraph applicationGraph) {
    super(xmlElement, parentFLow, applicationGraph);
    String targetFlowName = xmlElement.getAttribute("name").getValue();
    targetFlow = applicationGraph.getFlow(targetFlowName).orElse(null);
  }

  @Override
  public FlowComponent rewire(Deque<Flow> flowStack) {
    updatePropertiesContext();
    if (next().size() > 1) {
      LOGGER.warn("flowRefs can have at most a single return point");
    }

    // targetFlow exists and is a source-less flow or sub-flow
    if (!alreadyWired && targetFlow != null && !targetFlow.getComponents().isEmpty() &&
        !(targetFlow.getComponents().get(0) instanceof MessageSourceFlowComponent)) {

      returnFlowComponent = next().peek();
      ((BasicFlowComponent) returnFlowComponent).flowRefCaller = this;
      resetNext(targetFlow.getComponents().get(0));
      targetFlow.getComponents().get(targetFlow.getComponents().size() - 1).next(returnFlowComponent);
    }

    alreadyWired = true;
    return nextComponentToProcess(flowStack);
  }
}
