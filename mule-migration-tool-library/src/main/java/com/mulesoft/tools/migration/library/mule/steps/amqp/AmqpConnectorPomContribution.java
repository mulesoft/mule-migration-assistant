/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.amqp;

import static com.mulesoft.tools.migration.library.tools.PluginsVersions.targetVersion;

import com.mulesoft.tools.migration.project.model.pom.Dependency.DependencyBuilder;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

/**
 * Adds the AMQP Connector dependency
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class AmqpConnectorPomContribution implements PomContribution {

  @Override
  public String getDescription() {
    return "Add AMQP Connector dependency.";
  }

  @Override
  public void execute(PomModel object, MigrationReport report) throws RuntimeException {
    object.addDependency(new DependencyBuilder()
        .withGroupId("com.mulesoft.connectors")
        .withArtifactId("mule-amqp-connector")
        .withVersion(targetVersion("mule-amqp-connector"))
        .withClassifier("mule-plugin")
        .build());
  }

}
