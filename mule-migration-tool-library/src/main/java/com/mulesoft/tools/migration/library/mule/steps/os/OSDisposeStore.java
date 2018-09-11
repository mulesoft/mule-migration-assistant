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
 * Migrate OS Dispose Operation.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class OSDisposeStore extends AbstractOSMigrator {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri() = '" + OS_NAMESPACE_URI + "' and local-name() = 'dispose-store']";

  public OSDisposeStore() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    migrateOS(element);

    element.setName("clear");
    Attribute partition = element.getAttribute("partitionName");
    if (partition != null) {
      element.removeAttribute(partition);
    }

    report.report(WARN, element, element, "On Mule 4 the clear operation will clear the whole Object Store.",
                  "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-objectstore#dispose-clear");
  }
}
