/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.jms;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.library.mule.steps.jms.AbstractJmsEndpoint.JMS_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.jms.AbstractJmsEndpoint.JMS_NAMESPACE_URI;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrates the jms connector of the JMS transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class JmsConnector extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "/*/*[namespace-uri()='" + JMS_NAMESPACE_URI + "' and ("
      + "(local-name() = 'activemq-connector' or "
      + "local-name() = 'activemq-xa-connector' or "
      + "local-name() = 'weblogic-connector' or "
      + "local-name() = 'websphere-connector' or "
      + "local-name() = 'connector' or "
      + "local-name() = 'custom-connector'))]";

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
