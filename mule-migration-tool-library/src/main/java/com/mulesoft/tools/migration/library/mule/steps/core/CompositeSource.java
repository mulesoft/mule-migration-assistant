/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addElementBefore;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getContainerElement;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.ArrayList;

import org.jdom2.Element;

/**
 * Migrate composite sources
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class CompositeSource extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "/*/mule:flow/mule:composite-source";

  @Override
  public String getDescription() {
    return "Migrate composite sources";
  }

  public CompositeSource() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Element flow = getContainerElement(object);
    String flowName = flow.getAttributeValue("name");

    int i = 0;
    for (Element source : new ArrayList<>(object.getChildren())) {
      Element sourceFlow = new Element("flow", CORE_NAMESPACE).setAttribute("name", flowName + "_source" + (++i));
      addElementBefore(sourceFlow, flow);

      sourceFlow.addContent(source.detach());
      sourceFlow.addContent(new Element("flow-ref", CORE_NAMESPACE).setAttribute("name", flowName));
    }

    object.detach();
  }

}
