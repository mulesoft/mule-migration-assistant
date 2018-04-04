/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.step.other;

import java.util.HashSet;
import java.util.Set;

import com.mulesoft.tools.migration.engine.step.MigrationStep;
import com.mulesoft.tools.migration.engine.task.AbstractMigrationTask;
import com.mulesoft.tools.migration.engine.task.Version;
import com.mulesoft.tools.migration.project.structure.ProjectType;

/**
 * JUST A CONCEPT
 * todo delete
 * @author Mulesoft Inc.
 */
public class AnotherMigrationTask extends AbstractMigrationTask {

  @Override
  public Version getTo() {
    return null;
  }

  @Override
  public Version getFrom() {
    return null;
  }

  @Override
  public ProjectType getProjectType() {
    return null;
  }


  @Override
  public Set<MigrationStep> getSteps() {
    Set<MigrationStep> steps = new HashSet<>();

    return steps;
  }

  @Override
  public String getDescription() {
    return null;
  }
}
