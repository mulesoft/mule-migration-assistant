/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.amqp;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.library.mule.steps.amqp.AbstractAmqpEndpoint.AMQP_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.amqp.AbstractAmqpEndpoint.AMQP_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.mule.steps.amqp.AbstractAmqpEndpoint.AMQPS_NAMESPACE_URI;

import org.jdom2.Element;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

/**
 * Migrates the AMQP ack operation from the AMQP transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class AmqpAck extends AbstractApplicationModelMigrationStep implements ExpressionMigratorAware {

  private ExpressionMigrator expressionMigrator;

  public static final String XPATH_SELECTOR = "//*[(namespace-uri()='" + AMQP_NAMESPACE_URI
      + "' or namespace-uri()='" + AMQPS_NAMESPACE_URI + "') and local-name()='acknowledge-message']";

  @Override
  public String getDescription() {
    return "Update AMQP reject operation.";
  }

  public AmqpAck() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(AMQP_NAMESPACE));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.setNamespace(AMQP_NAMESPACE);
    object.setName("ack");
    object.setAttribute("ackId", "#[attributes.ackId]");
  }

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }

}
