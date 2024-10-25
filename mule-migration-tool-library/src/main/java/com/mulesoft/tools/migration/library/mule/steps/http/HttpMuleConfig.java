/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
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
