/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.obi.tools.migration.library.smartgate.steps.core;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Parent;

import java.util.List;

/**
 * Migrate flow-ref components
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RemoveBeforeAndAfterFlowRef extends AbstractApplicationModelMigrationStep {

  private static final String API_MAIN_AFTER_APIKIT_FLOW = "api-main-after-apikit-flow";
  private static final String API_MAIN_BEFORE_APIKIT_FLOW = "api-main-before-apikit-flow";
  public static final String XPATH_SELECTOR = getCoreXPathSelector("flow-ref");


  @Override
  public String getDescription() {
    return "Migrate flow-ref components";
  }

  public RemoveBeforeAndAfterFlowRef() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {

    if (API_MAIN_BEFORE_APIKIT_FLOW.equals(element.getAttributeValue("name"))) {
      final Parent parent = element.getParent();
      // parent.removeContent(element);
      /**
       * to set variable
       */

      element.setName("set-variable");
      element.setNamespace(CORE_NAMESPACE);
      final List<Attribute> attributes = element.getAttributes();
      for (Attribute attribute : attributes) {
        element.removeAttribute(attribute);

      }
      element.setAttribute("variableName", "smartgate_transaction_id");
      element.setAttribute("value", "#[correlationId]");
    }

    if (API_MAIN_AFTER_APIKIT_FLOW.equals(element.getAttributeValue("name"))) {
      /*
       * final Parent parent = element.getParent(); parent.removeContent(element);
       */
      Element parentElement = element.getParentElement();
      final Parent parent = parentElement.getParent();
      parent.removeContent(parentElement);
    }
  }
}
