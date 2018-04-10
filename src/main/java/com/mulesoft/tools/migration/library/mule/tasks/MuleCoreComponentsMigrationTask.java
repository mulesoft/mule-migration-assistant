/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.tasks;

import com.mulesoft.tools.migration.engine.step.MigrationStep;
import com.mulesoft.tools.migration.engine.task.AbstractMigrationTask;
import com.mulesoft.tools.migration.engine.task.Version;
import com.mulesoft.tools.migration.library.mule.steps.core.*;
import com.mulesoft.tools.migration.project.structure.ProjectType;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.mulesoft.tools.migration.library.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.library.util.MuleVersion.MULE_4_VERSION;
import static com.mulesoft.tools.migration.project.structure.ProjectType.MULE_FOUR_APPLICATION;

/**
 * Migration definition for Mule Core components
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MuleCoreComponentsMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Migrate Mule Core Components";
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
  public Set<MigrationStep> getSteps() {
    return newHashSet(new CatchExceptionStrategy(), new RemoveObjectToStringTransformer(),
                      new RollbackExceptionStrategy(), new ChoiceExceptionStrategy(),
                      new SetAttachment(), new SetProperty(), new TransactionalScope(),
                      new ExceptionStrategyRef(), new ForEachScope(), new RemoveJsonTransformerNamespace());
  }
}
