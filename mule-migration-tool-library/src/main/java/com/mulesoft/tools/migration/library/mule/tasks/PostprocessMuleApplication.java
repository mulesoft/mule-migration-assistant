/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.tasks;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.library.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.library.util.MuleVersion.MULE_4_VERSION;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;

import com.mulesoft.tools.migration.library.mule.steps.core.AttributesToInboundPropertiesScriptGenerator;
import com.mulesoft.tools.migration.library.mule.steps.core.CleanNamespaces;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;
import com.mulesoft.tools.migration.task.Version;

import java.util.List;

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
  public Version getTo() {
    return MULE_4_VERSION;
  }

  @Override
  public Version getFrom() {
    return MULE_3_VERSION;
  }

  @Override
  public ProjectType getProjectType() {
    return MULE_FOUR_APPLICATION;
  }

  @Override
  public List<MigrationStep> getSteps() {
    return newArrayList(new AttributesToInboundPropertiesScriptGenerator(),
                        new CleanNamespaces());
  }

}
