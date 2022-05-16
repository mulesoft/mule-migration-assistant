/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import org.jdom2.Element;

/**
 * Models a flow ref 
 *
 * @author Mulesoft Inc.
 * @since 1.3.0
 */
public class FlowRef extends MessageProcessor {

  private Flow source;
  private Flow destination;

  public FlowRef(Element xmlElement, Flow source, Flow destination, ApplicationGraph applicationGraph) {
    super(xmlElement, source, applicationGraph);
    this.source = source;
    this.destination = destination;
  }

  public Flow getDestinationFlow() {
    return destination;
  }
}
