/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.quartz;

import com.mulesoft.tools.migration.step.AbstractGlobalEndpointMigratorStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the global endpoints of the quartz transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class QuartzGlobalEndpoint extends AbstractGlobalEndpointMigratorStep {

  protected static final String QUARTZ_NS_PREFIX = "quartz";
  protected static final String QUARTZ_NS_URI = "http://www.mulesoft.org/schema/mule/quartz";

  public static final String XPATH_SELECTOR =
      "/*/*[namespace-uri() = '" + QUARTZ_NS_URI + "' and local-name() = 'endpoint']";

  @Override
  public String getDescription() {
    return "Update quartz global endpoints.";
  }

  public QuartzGlobalEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    doExecute(object, report);
  }

  @Override
  protected Namespace getNamespace() {
    return Namespace.getNamespace(QUARTZ_NS_PREFIX, QUARTZ_NS_URI);
  }

}
