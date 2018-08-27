/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import com.mulesoft.tools.migration.step.AbstractGlobalEndpointMigratorStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the global endpoints of the http transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpGlobalEndpoint extends AbstractGlobalEndpointMigratorStep {

  private static final String HTTP_NAMESPACE_PREFIX = "http";
  private static final String HTTP_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/http";
  public static final String XPATH_SELECTOR = "/*/http:endpoint";

  @Override
  public String getDescription() {
    return "Update HTTP global endpoints.";
  }

  public HttpGlobalEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);

  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    doExecute(object, report);
  }

  @Override
  protected Namespace getNamespace() {
    return Namespace.getNamespace(HTTP_NAMESPACE_PREFIX, HTTP_NAMESPACE_URI);
  }

}
