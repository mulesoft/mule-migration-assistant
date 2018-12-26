/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.jms;

import static com.mulesoft.tools.migration.library.mule.steps.jms.AbstractJmsEndpoint.JMS_NAMESPACE_URI;

import com.mulesoft.tools.migration.step.AbstractGlobalEndpointMigratorStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the global endpoints of the JMS transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class JmsGlobalEndpoint extends AbstractGlobalEndpointMigratorStep {

  public static final String XPATH_SELECTOR = "/*/*[namespace-uri()='" + JMS_NAMESPACE_URI + "' and local-name()='endpoint']";

  @Override
  public String getDescription() {
    return "Update JMS global endpoints.";
  }

  public JmsGlobalEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    doExecute(object, report);
  }

  @Override
  protected Namespace getNamespace() {
    return Namespace.getNamespace("jms", "http://www.mulesoft.org/schema/mule/jms");
  }

}
