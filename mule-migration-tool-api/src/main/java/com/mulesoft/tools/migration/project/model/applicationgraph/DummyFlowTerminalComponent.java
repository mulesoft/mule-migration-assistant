/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.MIGRATION_ID_ATTRIBUTE;

import com.mulesoft.tools.migration.step.util.XmlDslUtils;

import java.util.UUID;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Synthetic node added at the end of flows with message sources
 *
 * @author Mulesoft Inc.
 */
public class DummyFlowTerminalComponent extends BasicFlowComponent {

  private DummyFlowTerminalComponent(Element xmlElement, Flow parentFLow, ApplicationGraph applicationGraph) {
    super(xmlElement, parentFLow, applicationGraph);
  }

  public static DummyFlowTerminalComponent build(Flow parentFLow, ApplicationGraph applicationGraph) {
    Element element = new Element("dummy-flow-terminal-component");
    XmlDslUtils.addMigrationAttributeToElement(element,
                                               new Attribute(MIGRATION_ID_ATTRIBUTE, UUID.randomUUID().toString()));
    return new DummyFlowTerminalComponent(element, parentFLow, applicationGraph);
  }

}
