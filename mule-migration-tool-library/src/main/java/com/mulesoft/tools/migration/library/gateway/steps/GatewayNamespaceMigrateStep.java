/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_GATEWAY_NAMESPACE;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.NamespaceContribution;

/**
 * Contribute new gateway namespace into config files
 *
 * @author Mulesoft Inc.
 */
public class GatewayNamespaceMigrateStep implements NamespaceContribution {

  @Override
  public String getDescription() {
    return "Migrates the mule3 Namespaces to the mule 4 ones";
  }

  @Override
  public void execute(ApplicationModel applicationModel, MigrationReport migrationReport) throws RuntimeException {
    applicationModel.addNameSpace("api-gateway", MULE_4_GATEWAY_NAMESPACE.getURI(),
                                  "http://www.mulesoft.org/schema/mule/api-gateway/current/mule-api-gateway.xsd");
  }
}
