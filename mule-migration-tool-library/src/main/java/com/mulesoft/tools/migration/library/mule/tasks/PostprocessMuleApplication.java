/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.tasks;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;
import static java.util.Collections.singleton;

import com.mulesoft.tools.migration.library.mule.steps.core.AttributesToInboundPropertiesScriptGenerator;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.List;
import java.util.Set;

/**
 * Postprocess Mule Application Migration Task
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class PostprocessMuleApplication extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Postprocess the application";
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
  public Set<ProjectType> getApplicableProjectTypes() {
    return singleton(MULE_FOUR_APPLICATION);
  }

  @Override
  public List<MigrationStep> getSteps() {
    return newArrayList(new AttributesToInboundPropertiesScriptGenerator());
  }

}
