/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.apikit.tasks;

import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;

import com.mulesoft.tools.migration.library.apikit.steps.ApikitApiLocation;
import com.mulesoft.tools.migration.library.apikit.steps.ApikitErrorHandler;
import com.mulesoft.tools.migration.library.apikit.steps.ApikitHttpListenerMapping;
import com.mulesoft.tools.migration.library.apikit.steps.ApikitMigrationTaskPomContribution;
import com.mulesoft.tools.migration.library.apikit.steps.ApikitNamespace;
import com.mulesoft.tools.migration.library.apikit.steps.ApikitRouterConfig;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;
import com.mulesoft.tools.migration.step.MigrationStep;

import java.util.List;
import java.util.ArrayList;

/**
 * Migration Task for APIkit components
 *
 * @author Mulesoft Inc.
 */
public class ApikitMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Migrate APIkit Components";
  }

  @Override
  public String getTo() {
    return MULE_4_VERSION;
  }

  @Override
  public String getFrom() {
    return MULE_3_VERSION;
  }

  @Override
  public ProjectType getProjectType() {
    return MULE_FOUR_APPLICATION;
  }

  @Override
  public List<MigrationStep> getSteps() {
    List<MigrationStep> steps = new ArrayList<>();

    steps.add(new ApikitMigrationTaskPomContribution());
    steps.add(new ApikitNamespace());
    steps.add(new ApikitRouterConfig());
    steps.add(new ApikitHttpListenerMapping());
    steps.add(new ApikitErrorHandler());
    steps.add(new ApikitApiLocation());
    return steps;
  }
}
