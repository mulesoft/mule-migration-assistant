/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import org.jdom2.Element;

import java.util.Map;

/**
 * Models a mule message source
 *
 * @author Mulesoft Inc.
 * @since 1.3.0
 */
public class PropertiesSourceComponent extends MessageProcessor implements PropertiesSource, FlowComponent {

  private final SourceType type;

  public PropertiesSourceComponent(Element xmlElement, Flow parentFlow, ApplicationGraph applicationGraph) {
    super(xmlElement, parentFlow, applicationGraph);
    this.type = new SourceType(xmlElement.getNamespaceURI(), xmlElement.getName());
  }

  @Override
  public SourceType getType() {
    return type;
  }
}
