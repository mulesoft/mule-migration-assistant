/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.pom;

import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

/**
 * Set the project description with information about the migration
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SetProjectDescription implements PomContribution {


  @Override
  public String getDescription() {
    return "Set the project description ";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport report) throws RuntimeException {
    //TODO Find a way to dynamically obtain the runner version
    pomModel.setDescription("Application migrated with Mule Migration Assistant 1.0.0");
  }

}
