/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.google.common.collect.Lists.newArrayList;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the http and https connector of the http transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpConfig extends AbstractApplicationModelMigrationStep {

  private static final String HTTP_NAMESPACE_PREFIX = "http";
  private static final String HTTP_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/http";
  private static final Namespace HTTP_NAMESPACE = Namespace.getNamespace(HTTP_NAMESPACE_PREFIX, HTTP_NAMESPACE_URI);
  private static final String HTTPS_NAMESPACE_PREFIX = "https";
  private static final String HTTPS_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/https";
  private static final Namespace HTTPS_NAMESPACE = Namespace.getNamespace(HTTPS_NAMESPACE_PREFIX, HTTPS_NAMESPACE_URI);
  public static final String XPATH_SELECTOR =
      "/*/*[(namespace-uri() = '" + HTTP_NAMESPACE_URI + "' or namespace-uri() = '" + HTTPS_NAMESPACE_URI
          + "') and local-name() = 'connector']";

  @Override
  public String getDescription() {
    return "Update http and https connector config.";
  }

  public HttpConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(HTTP_NAMESPACE, HTTPS_NAMESPACE));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.detach();
  }
}
