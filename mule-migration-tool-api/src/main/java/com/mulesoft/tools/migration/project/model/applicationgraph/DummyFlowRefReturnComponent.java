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
 * Synthetic node added when flow-refs are the last element in a sub-flow
 * in order to have a return point in the same flow
 *
 * @author Mulesoft Inc.
 */
public class DummyFlowRefReturnComponent extends BasicFlowComponent {

  private DummyFlowRefReturnComponent(Element xmlElement, Flow parentFLow, ApplicationGraph applicationGraph) {
    super(xmlElement, parentFLow, applicationGraph);
  }

  public static DummyFlowRefReturnComponent build(Flow parentFLow, ApplicationGraph applicationGraph) {
    Element element = new Element("dummy-flow-ref-return-component");
    XmlDslUtils.addMigrationAttributeToElement(element,
                                               new Attribute(MIGRATION_ID_ATTRIBUTE, UUID.randomUUID().toString()));
    return new DummyFlowRefReturnComponent(element, parentFLow, applicationGraph);
  }

}
