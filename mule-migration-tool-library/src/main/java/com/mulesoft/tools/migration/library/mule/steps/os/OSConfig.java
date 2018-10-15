/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.os;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.mulesoft.tools.migration.step.category.MigrationReport;

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

