/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.NamespaceContribution;

/**
 * Adds the required HTTP Module Namespace
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpNamespaceContribution implements NamespaceContribution {

  @Override
  public String getDescription() {
    return "Add the HTTP Module namespace";
  }

  @Override
  public void execute(ApplicationModel object, MigrationReport report) throws RuntimeException {
    object.addNameSpace("http", "http://www.mulesoft.org/schema/mule/http",
                        "http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd");
  }

}
