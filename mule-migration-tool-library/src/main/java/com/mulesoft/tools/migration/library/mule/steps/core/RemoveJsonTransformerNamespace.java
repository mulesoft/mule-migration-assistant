/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.NamespaceContribution;

import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

/**
 * Remove Json to Object Namespace
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RemoveJsonTransformerNamespace implements NamespaceContribution {

  private static final String JSON_TRANSFORMER_XPRESSION = "//*[local-name()='json-to-object-transformer']";
  private static final String JSON_TRANSFORMER_NAME = "json";
  private static final String JSON_TRANSFORMER_URI = "http://www.mulesoft.org/schema/mule/json";
  private static final String JSON_TRANSFORMER_SCHEMA = "http://www.mulesoft.org/schema/mule/json/current/mule-json.xsd";

  @Override
  public String getDescription() {
    return "Remove Json to Object Namespace.";
  }

  @Override
  public void execute(ApplicationModel applicationModel, MigrationReport report) throws RuntimeException {
    try {
      XPathExpression xPathExpression = XPathFactory.instance().compile(JSON_TRANSFORMER_XPRESSION);
      if (applicationModel.getNodes(xPathExpression).isEmpty()) {
        applicationModel.removeNameSpace(JSON_TRANSFORMER_NAME, JSON_TRANSFORMER_URI, JSON_TRANSFORMER_SCHEMA);
      }
    } catch (Exception e) {
      throw new MigrationStepException("Fail to apply step. " + e.getMessage());
    }
  }
}
