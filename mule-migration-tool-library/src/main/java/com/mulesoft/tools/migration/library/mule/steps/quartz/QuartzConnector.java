/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.quartz;

import static java.util.Collections.singletonList;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Removes the connector of the quartz transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class QuartzConnector extends AbstractApplicationModelMigrationStep {

  protected static final String QUARTZ_NS_PREFIX = "quartz";
  protected static final String QUARTZ_NS_URI = "http://www.mulesoft.org/schema/mule/quartz";

  public static final String XPATH_SELECTOR =
      "/*/*[namespace-uri() = '" + QUARTZ_NS_URI + "' and local-name() = 'connector']";

  @Override
  public String getDescription() {
    return "Removes the connector of the quartz transport.";
  }

  public QuartzConnector() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(singletonList(getNamespace(QUARTZ_NS_PREFIX, QUARTZ_NS_URI)));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    report.report("quartz.connector", object, object.getParentElement());
    object.detach();
  }
}
