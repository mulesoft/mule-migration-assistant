/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import org.jdom2.Element;

/**
 * Models a mule message processor
 *
 * @author Mulesoft Inc.
 * @since 1.3.0
 */
public class MessageProcessor implements FlowComponent {

  private final String name;
  private Element xmlElement;
  private Flow parentFLow;
  private PropertiesMigrationContext propertiesMigrationContext;

  public MessageProcessor(Element xmlElement, Flow parentFLow) {
    this.xmlElement = xmlElement;
    this.parentFLow = parentFLow;
    this.name = String.format("%s:%s", xmlElement.getNamespace().getURI(), xmlElement.getName());
  }

  public Element getXmlElement() {
    return xmlElement;
  }

  @Override
  public String getName() {
    return this.name;
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
