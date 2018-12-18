/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.security.crc32;

import static com.mulesoft.tools.migration.library.tools.PluginsVersions.targetVersion;

import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.Dependency.DependencyBuilder;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

/**
 * Adds the mule-cryptography-module dependency
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ChecksumPomContribution implements PomContribution {

  @Override
  public String getDescription() {
    return "Add mule-cryptography-module dependency.";
  }

  @Override
  public void execute(PomModel object, MigrationReport report) throws RuntimeException {
    object.addDependency(new Dependency.DependencyBuilder()
        .withGroupId("com.mulesoft.modules")
        .withArtifactId("mule-cryptography-module")
        .withVersion(targetVersion("mule-cryptography-module"))
        .withClassifier("mule-plugin")
        .build());

    object.removeDependency(new DependencyBuilder()
        .withGroupId("com.mulesoft.security")
        .withArtifactId("mule-module-security-crc32")
        .withVersion("*")
        .build());

  }

}
