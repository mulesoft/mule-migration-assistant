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
 * Migrate OS Store Operation.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class OSStore extends AbstractOSMigrator {

  public static final String XPATH_SELECTOR = "//*[namespace-uri() = '" + OS_NAMESPACE_URI + "' and local-name() = 'store']";

  public OSStore() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    migrateOS(element);


    Attribute overwriteAtt = element.getAttribute("overwrite");
    if (overwriteAtt == null) {
      element.setAttribute(new Attribute("failIfPresent", "true"));
    } else {
      if (overwriteAtt.getValue().equals("false")) {
        element.setAttribute(new Attribute("failIfPresent", "true"));
      }
      element.removeAttribute(overwriteAtt);
    }

    Attribute valueAtt = element.getAttribute("value-ref");
    if (valueAtt != null) {
      Element childValue = new Element("value", NEW_OS_NAMESPACE);
      childValue.addContent(new CDATA(getExpressionMigrator().migrateExpression(valueAtt.getValue(), true, element)));
      element.addContent(childValue);
      element.removeAttribute(valueAtt);
    }
  }
}
