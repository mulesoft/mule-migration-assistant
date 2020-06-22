/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.vm;

import static com.mulesoft.tools.migration.library.tools.PluginsVersions.targetVersion;

import com.mulesoft.tools.migration.project.model.pom.Dependency.DependencyBuilder;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

/**
 * Adds the VM Connector dependency
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class VmConnectorPomContribution implements PomContribution {

  @Override
  public String getDescription() {
    return "Add VM Connector dependency.";
  }

  @Override
  public void execute(PomModel object, MigrationReport report) throws RuntimeException {
    addVMDependency(object);
  }

  public static void addVMDependency(PomModel pomModel) {
    pomModel.addDependency(new DependencyBuilder()
        .withGroupId("org.mule.connectors")
        .withArtifactId("mule-vm-connector")
        .withVersion(targetVersion("mule-vm-connector"))
        .withClassifier("mule-plugin")
        .build());
  }

}
