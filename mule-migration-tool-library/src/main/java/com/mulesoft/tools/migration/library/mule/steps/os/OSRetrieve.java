/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.os;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.CDATA;
import org.jdom2.Element;

/**
 * Migrate OS Retrieve operation.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class OSRetrieve extends AbstractOSMigrator {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri() = '" + OS_NAMESPACE_URI + "' and local-name() = 'retrieve']";

  public OSRetrieve() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public String getDescription() {
    return "Update Object Store connector.";
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    migrateOS(element);

    element.removeAttribute("targetScope");

    Attribute targetProp = element.getAttribute("targetProperty");
    if (targetProp != null) {
      targetProp.setName("target");
    }

    Attribute defaultValue = element.getAttribute("defaultValue-ref");
    if (defaultValue != null) {
      Element childValue = new Element("default-value", NEW_OS_NAMESPACE);
      childValue.addContent(new CDATA(getExpressionMigrator().migrateExpression(defaultValue.getValue(), true, element)));
      element.addContent(childValue);
      element.removeAttribute(defaultValue);
    }
  }

}
