/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.library.mule.steps.http.AbstractHttpConnectorMigrationStep.HTTP_NAMESPACE_URI;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrates the http elements in the application config
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpMuleConfig extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR =
      "/*/mule:configuration/*[namespace-uri()='" + HTTP_NAMESPACE_URI + "' and local-name()='config']";

  @Override
  public String getDescription() {
    return "Update elements in the application config.";
  }

  public HttpMuleConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.detach();
  }
}
