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
public class PropertiesSourceComponent implements PropertiesSource, FlowComponent {

  private final Element elementXml;
  private final SourceType type;
  private final Flow parentFlow;
  private PropertiesMigrationContext propertiesMigrationContext;

  public PropertiesSourceComponent(Element xmlElement, Flow parentFlow) {
    this.elementXml = xmlElement;
    this.type = new SourceType(xmlElement.getNamespaceURI(), xmlElement.getName());
    this.parentFlow = parentFlow;
  }

  public Element getXmlElement() {
    return this.elementXml;
  }


  @Override
  public SourceType getType() {
    return type;
  }

  @Override
  public Flow getParentFlow() {
    return parentFlow;
  }

  @Override
  public PropertiesMigrationContext getPropertiesMigrationContext() {
    return this.propertiesMigrationContext;
  }

  public void setPropertiesMigrationContext(PropertiesMigrationContext propertiesMigrationContext) {
    this.propertiesMigrationContext = propertiesMigrationContext;
  }

}
