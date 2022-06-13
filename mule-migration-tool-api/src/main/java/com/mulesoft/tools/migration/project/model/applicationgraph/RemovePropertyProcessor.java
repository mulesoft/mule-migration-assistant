/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import org.jdom2.Element;

/**
 * Models a remove-property mule message processor
 *
 * @author Mulesoft Inc.
 * @since 1.4.0
 */
public class RemovePropertyProcessor extends AbstractPropertyProcessor {

  String originalPropertyName;

  public RemovePropertyProcessor(Element xmlElement, Flow parentFLow,
                                 ApplicationGraph graph) {
    super(xmlElement, parentFLow, graph);
    this.originalPropertyName = xmlElement.getAttributeValue("propertyName");
  }

  @Override
  public void accept(FlowComponentVisitor visitor) {
    visitor.visitRemovePropertyProcessor(this);
  }

  public String getOriginalPropertyName() {
    return originalPropertyName;
  }
}

