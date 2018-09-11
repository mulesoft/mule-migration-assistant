/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.os;

import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Attribute;
import org.jdom2.Element;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;

/**
 * Migrates OS Configuration
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class OSConfig extends AbstractOSMigrator {

  public static final String XPATH_SELECTOR = "//*[namespace-uri() = '" + OS_NAMESPACE_URI + "' and local-name() = 'config']";

  public OSConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    migrateOS(element);
    element.setName("object-store");
    element.removeAttribute("partition");
    Attribute persistent = element.getAttribute("persistent");
    if (persistent == null) {
      element.setAttribute(new Attribute("persistent", "false"));
    }

    Attribute config = element.getAttribute("objectStore-ref");
    if (config != null) {
      report.report(WARN, element, element, "On Mule 4 you no longer need to create a spring bean to declare an Object Store.",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-objectstore#custom-object-store");
      element.removeAttribute(config);
    }
  }
}

