/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.os;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;

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

    report.report("os.disposeStore", element, element);
  }
}
