/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.amqp;

import static com.mulesoft.tools.migration.library.mule.steps.amqp.AbstractAmqpEndpoint.AMQP_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.mule.steps.amqp.AbstractAmqpEndpoint.AMQPS_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.mule.steps.amqp.AbstractAmqpEndpoint.AMQP_NAMESPACE;

import com.mulesoft.tools.migration.step.AbstractGlobalEndpointMigratorStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the global endpoints of the AMQP transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class AmqpGlobalEndpoint extends AbstractGlobalEndpointMigratorStep {

  public static final String XPATH_SELECTOR = "/*/*[(namespace-uri()='" + AMQP_NAMESPACE_URI + "' or namespace-uri()='"
      + AMQPS_NAMESPACE_URI + "') and local-name()='endpoint']";

  @Override
  public String getDescription() {
    return "Update AMQP global endpoints.";
  }

  public AmqpGlobalEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    doExecute(object, report);
  }

  @Override
  protected Namespace getNamespace() {
    return AMQP_NAMESPACE;
  }

}
