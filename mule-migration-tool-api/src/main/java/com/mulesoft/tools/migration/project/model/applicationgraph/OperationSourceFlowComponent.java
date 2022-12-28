/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import java.util.Deque;

import org.jdom2.Element;

/**
 * Models a mule operation which creates a new properties context
 *
 * @author Mulesoft Inc.
 */
public class OperationSourceFlowComponent extends BasicFlowComponent {

  public OperationSourceFlowComponent(Element xmlElement, Flow parentFLow, ApplicationGraph applicationGraph,
                                      SourceType sourceType) {
    super(xmlElement, parentFLow, applicationGraph);
    this.outputContext = new PropertiesMigrationContext(applicationGraph.getInboundTranslator(), sourceType);
  }

  @Override
  public FlowComponent rewire(Deque<Flow> flowStack) {
    updatePropertiesContext(outputContext);
    return nextComponentToProcess(flowStack);
  }

}
