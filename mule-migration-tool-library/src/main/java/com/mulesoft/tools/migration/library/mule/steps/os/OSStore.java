/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
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
      addFailAttribute(element, report);
    } else {
      if (overwriteAtt.getValue().equals("false")) {
        addFailAttribute(element, report);
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

  private void addFailAttribute(Element element, MigrationReport report) {
    element.setAttribute(new Attribute("failIfPresent", "true"));
    report.report("os.store", element, element);
  }

}
