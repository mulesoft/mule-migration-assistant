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

import com.mulesoft.tools.migration.library.mule.steps.core.CatchExceptionStrategy;
import com.mulesoft.tools.migration.library.mule.steps.core.ChoiceExceptionStrategy;
import com.mulesoft.tools.migration.library.mule.steps.core.CompatibilityPomContribution;
import com.mulesoft.tools.migration.library.mule.steps.core.CopyProperties;
import com.mulesoft.tools.migration.library.mule.steps.core.ExceptionStrategyRef;
import com.mulesoft.tools.migration.library.mule.steps.core.Flow;
import com.mulesoft.tools.migration.library.mule.steps.core.ForEachScope;
import com.mulesoft.tools.migration.library.mule.steps.core.MessagePropertiesTransformer;
import com.mulesoft.tools.migration.library.mule.steps.core.Poll;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveJsonTransformerNamespace;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveObjectToStringTransformer;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveProperty;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveSchedulersNamespace;
import com.mulesoft.tools.migration.library.mule.steps.core.RollbackExceptionStrategy;
import com.mulesoft.tools.migration.library.mule.steps.core.SetAttachment;
import com.mulesoft.tools.migration.library.mule.steps.core.SetProperty;
import com.mulesoft.tools.migration.library.mule.steps.core.TransactionalScope;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;
import com.mulesoft.tools.migration.task.Version;

import java.util.List;

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
  public List<MigrationStep> getSteps() {
    return newArrayList(new CompatibilityPomContribution(),
                        new CatchExceptionStrategy(),
                        new RemoveObjectToStringTransformer(),
                        new RemoveObjectToStringTransformer(),
                        new RollbackExceptionStrategy(),
                        new ChoiceExceptionStrategy(),
                        new SetAttachment(),
                        new SetProperty(),
                        new CopyProperties(),
                        new RemoveProperty(),
                        new TransactionalScope(),
                        new ExceptionStrategyRef(),
                        new ForEachScope(),
                        new RemoveJsonTransformerNamespace(),
                        new MessagePropertiesTransformer(),
                        new Flow(),
                        new Poll(),
                        new RemoveSchedulersNamespace());
  }
}
