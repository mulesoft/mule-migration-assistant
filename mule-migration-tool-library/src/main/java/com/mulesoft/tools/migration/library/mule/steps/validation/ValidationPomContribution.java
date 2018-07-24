/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.validation;

import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

/**
 * Add validation module dependency on pom.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ValidationPomContribution implements PomContribution {

  @Override
  public String getDescription() {
    return "Add scripting module to pom.";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport report) throws RuntimeException {
    pomModel.addDependency(new Dependency.DependencyBuilder()
        .withGroupId("org.mule.modules")
        .withArtifactId("mule-validation-module")
        .withVersion("1.2.1")
        .withClassifier("mule-plugin")
        .build());
  }
}
