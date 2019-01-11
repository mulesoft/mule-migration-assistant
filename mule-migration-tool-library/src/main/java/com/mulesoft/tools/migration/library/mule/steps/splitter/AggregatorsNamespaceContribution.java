/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.splitter;

import static com.mulesoft.tools.migration.library.mule.steps.splitter.AbstractSplitter.AGGREGATORS_NAMESPACE;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.NamespaceContribution;

public class AggregatorsNamespaceContribution implements NamespaceContribution {

  private static final String AGGREGATORS_SCHEMA_LOCATION =
      "http://www.mulesoft.org/schema/mule/aggregators/current/mule-aggregators.xsd";

  @Override
  public String getDescription() {
    return "Add aggregators namespace contribution";
  }

  @Override
  public void execute(ApplicationModel object, MigrationReport report) throws RuntimeException {
    object.addNameSpace(AGGREGATORS_NAMESPACE.getPrefix(), AGGREGATORS_NAMESPACE.getURI(), AGGREGATORS_SCHEMA_LOCATION);
  }
}
