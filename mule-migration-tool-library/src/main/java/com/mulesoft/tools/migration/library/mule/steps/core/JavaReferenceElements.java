/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Remove elements from 3.x that required references to Java to work
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class JavaReferenceElements extends AbstractApplicationModelMigrationStep {

  // custom-filter already has its own migrator
  // custom-security-provider and custom-encryption-strategy still exist
  public static final String XPATH_SELECTOR = ""
      + "//*["
      + "local-name()='component' or "
      + "local-name()='pooled-component' or "
      + "local-name()='custom-agent' or "
      + "local-name()='custom-queue-store' or "
      + "local-name()='custom-processor' or " // actually deprecated
      + "local-name()='custom-source' or "
      + "local-name()='custom-entry-point-resolver-set' or "
      + "local-name()='custom-entry-point-resolver' or "
      + "local-name()='reconnect-custom-strategy' or "
      + "local-name()='reconnect-custom-notifier' or "
      + "local-name()='custom-service' or "
      + "local-name()='custom-processing-strategy' or "
      + "local-name()='custom-transaction-manager' or "
      + "local-name()='custom-security-filter' or "
      + "local-name()='custom-interceptor' or "
      + "local-name()='custom-transformer' or " // actually deprecated
      + "local-name()='custom-exception-strategy' or "
      + "local-name()='custom-connector' or "
      + "local-name()='custom-object-store' or "
      + "local-name()='custom-aggregator' or "
      + "local-name()='custom-splitter' or "
      + "local-name()='custom-router' or "
      + "local-name()='custom-correlation-aggregator-router' or "
      + "local-name()='custom-inbound-router' or "
      + "local-name()='custom-async-reply-router' or "
      + "local-name()='custom-outbound-router' or "
      + "local-name()='custom-catch-all-strategy' or "
      + "local-name()='custom-forwarding-catch-all-strategy' or "
      + "local-name()='custom-message-info-mapping' or "
      + "local-name()='custom-lifecycle-adapter-factory'"
      + "]";

  @Override
  public String getDescription() {
    return "Remove elements from 3.x that required referecces to Java to work";
  }

  public JavaReferenceElements() {
    this.setAppliedTo(XPATH_SELECTOR);
  }


  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    report.report("components.java", object, object, object.getName());
  }

}
