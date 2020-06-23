/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.security.filter;

import com.mulesoft.tools.migration.library.mule.steps.validation.ValidationPomContribution;
import com.mulesoft.tools.migration.project.model.pom.Dependency.DependencyBuilder;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

/**
 * Add validation module dependency on pom.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SecurityFiltersPomContribution extends ValidationPomContribution {

  @Override
  public String getDescription() {
    return "Add validation module dependency on pom.";
  }

  @Override
  public void execute(PomModel object, MigrationReport report) throws RuntimeException {
    super.execute(object, report);

    object.removeDependency(new DependencyBuilder()
        .withGroupId("com.mulesoft.security")
        .withArtifactId("mule-module-security-filters")
        .withVersion("*")
        .build());

  }

}
