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
      report.report("os.config", element, element);
      element.removeAttribute(config);
    }
  }
}

