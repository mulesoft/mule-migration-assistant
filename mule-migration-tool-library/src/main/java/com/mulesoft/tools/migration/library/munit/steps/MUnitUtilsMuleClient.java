/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.munit.steps;

import static com.google.common.collect.Lists.newArrayList;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Remove MUnit MClient processor.
 * @author Mulesoft Inc.
 */
public class MUnitUtilsMuleClient extends AbstractApplicationModelMigrationStep {

  private static final String MCLIENT_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/mclient";
  private static final String MCLIENT_NAMESPACE_PREFIX = "mclient";
  private static final Namespace MCLIENT_NAMESPACE = Namespace.getNamespace(MCLIENT_NAMESPACE_PREFIX, MCLIENT_NAMESPACE_URI);

  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + MCLIENT_NAMESPACE_URI + "']";

  @Override
  public String getDescription() {
    return "Remove MClient processor";
  }

  public MUnitUtilsMuleClient() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(MCLIENT_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {

    if (element.getParentElement().getName().equals("test")) {
      report.report("munit.mclient", element, element.getParentElement());
    }
    element.detach();
  }

}
