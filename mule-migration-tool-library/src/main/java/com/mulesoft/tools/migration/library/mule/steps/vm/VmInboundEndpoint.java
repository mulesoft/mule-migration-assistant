/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.vm;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateSourceStructure;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the inbound endpoint of the VM Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class VmInboundEndpoint extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  private static final String VM_NAMESPACE_PREFIX = "vm";
  private static final String VM_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/transport/vm";
  private static final Namespace VM_NAMESPACE = Namespace.getNamespace(VM_NAMESPACE_PREFIX, VM_NAMESPACE_URI);
  public static final String XPATH_SELECTOR = "/mule:mule/mule:flow/vm:inbound-endpoint";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update VM transport inbound endpoint.";
  }

  public VmInboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(VM_NAMESPACE));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    // This is a temporary simple implementation for getting the outbound properties in variables
    object.setAttribute("isMessageSource", "true");
    migrateSourceStructure(getApplicationModel(), object, report, false);
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
