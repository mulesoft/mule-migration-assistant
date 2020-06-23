/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
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
