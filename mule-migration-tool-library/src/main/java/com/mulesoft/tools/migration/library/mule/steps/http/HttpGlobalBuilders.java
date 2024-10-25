/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
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
public class HttpGlobalBuilders extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR =
      "/*/*[namespace-uri() = 'http://www.mulesoft.org/schema/mule/http' and (local-name() = 'response-builder' or local-name() = 'request-builder')]";

  @Override
  public String getDescription() {
    return "Update http and https connector config.";
  }

  public HttpGlobalBuilders() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.detach();
  }
}
