/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.jms;

import static com.google.common.collect.Lists.newArrayList;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the vm connector of the JMS transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class JmsConnector extends AbstractApplicationModelMigrationStep {

  private static final String JMS_NAMESPACE_PREFIX = "jms";
  private static final String JMS_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/jms";
  private static final Namespace JMS_NAMESPACE = Namespace.getNamespace(JMS_NAMESPACE_PREFIX, JMS_NAMESPACE_URI);

  public static final String XPATH_SELECTOR =
      "/mule:mule/*[namespace-uri() = '" + JMS_NAMESPACE_URI + "' and local-name() = 'connector']";

  @Override
  public String getDescription() {
    return "Update JMS connector config.";
  }

  public JmsConnector() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(JMS_NAMESPACE));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.detach();
  }
}
