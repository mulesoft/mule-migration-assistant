/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrates the http and https connector of the http transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpConfig extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR =
      "/mule:mule/*[(namespace-uri() = 'http://www.mulesoft.org/schema/mule/http' or namespace-uri() = 'http://www.mulesoft.org/schema/mule/https') and local-name() = 'connector']";

  @Override
  public String getDescription() {
    return "Update http and https connector config.";
  }

  public HttpConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.detach();
  }
}
