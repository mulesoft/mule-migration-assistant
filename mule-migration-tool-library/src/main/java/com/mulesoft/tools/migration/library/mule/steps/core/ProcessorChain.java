/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Element;

import java.util.concurrent.atomic.AtomicInteger;

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

  private static final String UNNAMED_PROCESSOR_CHAIN_PREFIX = "MMA_processor_chain_";
  private final AtomicInteger unnamedProcessorChainIndex = new AtomicInteger();

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
    if (name == null) {
      name = UNNAMED_PROCESSOR_CHAIN_PREFIX + unnamedProcessorChainIndex.incrementAndGet();
    }
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
