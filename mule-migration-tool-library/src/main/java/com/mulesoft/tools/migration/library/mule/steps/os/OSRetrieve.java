/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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
