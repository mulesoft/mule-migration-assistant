/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import org.jdom2.Element;

/**
 * Models a mule message processor that is only synthetic and does not exist in the real app.
 * Used for holding the context for response on transports or connectors when there is not an explicit xml element for them
 *
 * @author Mulesoft Inc.
 * @since 1.3.0
 */
public class SyntheticMessageProcessor extends MessageProcessor {

  private static final String NON_MATCHING_ID = "NON_MATCHING";

  public SyntheticMessageProcessor(Element parentElementName, String elementSuffix, Flow parentFLow, ApplicationGraph graph) {
    super(parentFLow);
    this.name =
        super.getComponentName(parentElementName + elementSuffix, parentElementName.getNamespacePrefix(), parentFLow, graph);
  }

  @Override
  public void accept(FlowComponentVisitor visitor) {
    visitor.visitMessageProcessor(this);
  }

  @Override
  public String getElementId() {
    return NON_MATCHING_ID;
  }

  @Override
  public Flow getParentFlow() {
    return parentFLow;
  }

  @Override
  public PropertiesMigrationContext getPropertiesMigrationContext() {
    return this.propertiesMigrationContext;
  }

  public void setPropertiesMigrationContext(PropertiesMigrationContext propertiesMigrationContext) {
    this.propertiesMigrationContext = propertiesMigrationContext;
  }
}
