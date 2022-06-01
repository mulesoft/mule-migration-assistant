/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import org.jdom2.Element;

/**
 * Models a set-property mule message processor
 *
 * @author Mulesoft Inc.
 * @since 1.4.0
 */
public class SetPropertyProcessor extends MessageProcessor {

  public static String OUTBOUND_PREFIX = "outbound_";
  private final String propertyName;

  public SetPropertyProcessor(Element xmlElement, Flow parentFLow,
                              ApplicationGraph graph) {
    super(xmlElement, parentFLow, graph);
    this.propertyName = xmlElement.getAttribute("propertyName").getValue();
  }

  public String getPropertyName() {
    return this.propertyName;
  }

  public String getPropertyTranslation() {
    return String.format("vars.%s%s", OUTBOUND_PREFIX, this.propertyName);
  }

  @Override
  public void accept(FlowComponentVisitor visitor) {
    visitor.visitSetPropertyProcessor(this);
  }
}
