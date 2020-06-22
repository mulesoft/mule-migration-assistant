/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.jms;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrates the bti:xa-caching-connection-factory of the JMS transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class BtiXaCachingConnectionFactory extends AbstractApplicationModelMigrationStep {

  @Override
  public String getDescription() {
    return "Update BTI xa-caching-connection-factory connector config.";
  }

  public BtiXaCachingConnectionFactory() {
    this.setAppliedTo("/*/*[namespace-uri() = 'http://www.mulesoft.org/schema/mule/ee/bti' and local-name() = 'xa-caching-connection-factory']");
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    if (object.getAttribute("minPoolSize") != null
        || object.getAttribute("maxPoolSize") != null
        || object.getAttribute("maxIdleTime") != null) {
      report.report("jms.xaConnectionCache", object, object.getParentElement());
    }

    object.detach();
  }

}
