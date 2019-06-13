/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Element;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NS_URI;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementAfter;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.isTopLevelElement;

/**
 * Migration step for Processor Chain component
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ProcessorChain extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + CORE_NS_URI + "' and local-name()='processor-chain']";

  @Override
  public String getDescription() {
    return "Update Processor Chain component.";
  }

  public ProcessorChain() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    String name = element.getAttributeValue("name");
    Element subFlow = new Element("sub-flow", CORE_NAMESPACE).setAttribute("name", name);
    subFlow.addContent(element.cloneContent());

    addTopLevelElement(subFlow, element.getDocument());

    if (!isTopLevelElement(element)) {
      Element flowRef = new Element("flow-ref", CORE_NAMESPACE).setAttribute("name", name);
      addElementAfter(flowRef, element);
    }
    element.detach();
  }

}
