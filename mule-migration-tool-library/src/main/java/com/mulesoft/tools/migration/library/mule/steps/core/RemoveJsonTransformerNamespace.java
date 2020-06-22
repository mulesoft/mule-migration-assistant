/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.library.mule.steps.json.JsonMigrationStep.JSON_NAMESPACE;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.NamespaceContribution;

/**
 * Remove Json to Object Namespace
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RemoveJsonTransformerNamespace implements NamespaceContribution {

  private static final String JSON_TRANSFORMER_XPRESSION =
      "//*[namespace-uri()='" + JSON_NAMESPACE.getURI() + "' and local-name()='json-to-object-transformer']";
  private static final String JSON_TRANSFORMER_SCHEMA = "http://www.mulesoft.org/schema/mule/json/current/mule-json.xsd";

  @Override
  public String getDescription() {
    return "Remove Json to Object Namespace.";
  }

  @Override
  public void execute(ApplicationModel applicationModel, MigrationReport report) throws RuntimeException {
    try {
      if (applicationModel.getNodes(JSON_TRANSFORMER_XPRESSION).isEmpty()) {
        applicationModel.removeNameSpace(JSON_NAMESPACE.getPrefix(), JSON_NAMESPACE.getURI(), JSON_TRANSFORMER_SCHEMA);
      }
    } catch (Exception e) {
      throw new MigrationStepException("Fail to apply step. " + e.getMessage());
    }
  }
}
