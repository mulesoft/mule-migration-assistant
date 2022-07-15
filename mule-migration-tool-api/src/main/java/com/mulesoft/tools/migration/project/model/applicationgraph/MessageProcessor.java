/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.MIGRATION_ID_ATTRIBUTE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.MIGRATION_NAMESPACE;

import java.util.List;

import org.jdom2.Element;

/**
 * Models a mule message processor
 *
 * @author Mulesoft Inc.
 * @since 1.3.0
 */
public class MessageProcessor implements FlowComponent {

  protected String name;
  private Element xmlElement;
  protected Flow parentFLow;
  private String elementId;
  protected PropertiesMigrationContext propertiesMigrationContext;

  public MessageProcessor(Flow parentFLow) {
    this.parentFLow = parentFLow;
  }

  public MessageProcessor(Element xmlElement, Flow parentFLow, ApplicationGraph graph) {
    this.xmlElement = xmlElement;
    this.parentFLow = parentFLow;
    this.elementId = xmlElement.getAttributeValue(MIGRATION_ID_ATTRIBUTE, MIGRATION_NAMESPACE);
    name = getComponentName(xmlElement.getName(), xmlElement.getNamespace().getPrefix(), parentFLow, graph);
  }

  protected String getComponentName(String elementName, String namespacePrefix, Flow parentFLow, ApplicationGraph graph) {
    String potentialName =
        String.format("%s%s_%s", namespacePrefix.isEmpty() ? "" : namespacePrefix + "_", elementName, parentFLow.getName());
    List<String> matchingNames = graph.getAllVertexNamesWithBaseName(potentialName);
    if (!matchingNames.isEmpty()) {
      String lastMatchingElementName = matchingNames.get(matchingNames.size() - 1);
      String counter = "";
      if (!lastMatchingElementName.equals(potentialName)) {
        counter = lastMatchingElementName.replaceFirst("(.+-)([0-9]*)", "$2");
      }
      counter = counter.isEmpty() ? "0" : counter;
      potentialName = String.format("%s-%s", potentialName, Integer.valueOf(counter) + 1);
    }
    return potentialName;
  }

  public Element getXmlElement() {
    return xmlElement;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public void accept(FlowComponentVisitor visitor) {
    visitor.visitMessageProcessor(this);
  }

  @Override
  public String getElementId() {
    return this.elementId;
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
