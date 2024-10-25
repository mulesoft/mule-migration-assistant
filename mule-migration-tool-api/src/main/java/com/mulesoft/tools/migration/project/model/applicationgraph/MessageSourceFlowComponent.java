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
 * Models a message source component
 *
 * @author Mulesoft Inc.
 */
public class MessageSourceFlowComponent extends BasicFlowComponent {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageSourceFlowComponent.class);

  private FlowComponent terminalComponent;

  public MessageSourceFlowComponent(Element xmlElement, Flow parentFLow, ApplicationGraph applicationGraph,
                                    SourceType sourceType) {
    super(xmlElement, parentFLow, applicationGraph);
    this.outputContext = new PropertiesMigrationContext(applicationGraph.getInboundTranslator(), sourceType);
  }

  @Override
  public FlowComponent rewire(Deque<Flow> flowStack) {
    if (!previous.isEmpty()) {
      LOGGER.warn("no previous nodes allowed for message source");
    }
    if (next.size() > 1) {
      LOGGER.warn("specialized rewiring required: more than one next nodes");
    }
    return nextComponentToProcess(flowStack);
  }

  public void setTerminalComponent(FlowComponent terminal) {
    this.terminalComponent = terminal;
    this.inputContext = terminalComponent.getPropertiesMigrationContext();
  }
}
