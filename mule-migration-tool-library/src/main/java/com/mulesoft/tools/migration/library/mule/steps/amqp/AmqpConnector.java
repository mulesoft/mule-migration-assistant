/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.amqp;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.library.mule.steps.amqp.AbstractAmqpEndpoint.AMQP_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.amqp.AbstractAmqpEndpoint.AMQP_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.mule.steps.amqp.AbstractAmqpEndpoint.AMQPS_NAMESPACE_URI;

import org.jdom2.Element;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

/**
 * Migrates the AMQP connector from the AMQP transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class AmqpConnector extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR =
      "/*/*[(namespace-uri()='" + AMQP_NAMESPACE_URI + "' or namespace-uri()='" + AMQPS_NAMESPACE_URI + "') and ("
          + "(local-name() =  'connector'))]";

  @Override
  public String getDescription() {
    return "Update AMQP connector config.";
  }

  public AmqpConnector() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(AMQP_NAMESPACE));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.detach();
  }
}
