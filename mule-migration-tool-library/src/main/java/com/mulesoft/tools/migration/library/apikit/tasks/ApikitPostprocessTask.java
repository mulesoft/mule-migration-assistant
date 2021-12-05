/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.apikit.tasks;

import com.mulesoft.tools.migration.library.apikit.steps.*;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.step.MigrationStep;

import java.util.ArrayList;
import java.util.List;

import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

/**
 * Post Migration Task for APIkit components
 *
 * @author Mulesoft Inc.
 */
public class ApikitPostprocessTask extends com.mulesoft.tools.migration.task.AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Postprocess steps related to APIKit";
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
    List<MigrationStep> result = new ArrayList<>();
    result.add(new ApikitUriParams());
    return result;
  }

}
