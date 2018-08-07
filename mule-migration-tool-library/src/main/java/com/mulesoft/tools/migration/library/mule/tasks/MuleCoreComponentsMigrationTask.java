/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.tasks;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

import com.mulesoft.tools.migration.library.mule.steps.core.CatchExceptionStrategy;
import com.mulesoft.tools.migration.library.mule.steps.core.ChoiceExceptionStrategy;
import com.mulesoft.tools.migration.library.mule.steps.core.ChoiceExpressions;
import com.mulesoft.tools.migration.library.mule.steps.core.CompatibilityPomContribution;
import com.mulesoft.tools.migration.library.mule.steps.core.ExceptionStrategyRef;
import com.mulesoft.tools.migration.library.mule.steps.core.Flow;
import com.mulesoft.tools.migration.library.mule.steps.core.ForEachExpressions;
import com.mulesoft.tools.migration.library.mule.steps.core.ForEachScope;
import com.mulesoft.tools.migration.library.mule.steps.core.GenericGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.core.Poll;
import com.mulesoft.tools.migration.library.mule.steps.core.RollbackExceptionStrategy;
import com.mulesoft.tools.migration.library.mule.steps.core.ScatterGather;
import com.mulesoft.tools.migration.library.mule.steps.core.SetPayload;
import com.mulesoft.tools.migration.library.mule.steps.core.SetVariable;
import com.mulesoft.tools.migration.library.mule.steps.core.TransactionalScope;
import com.mulesoft.tools.migration.library.mule.steps.ee.CacheHttpCachingStrategy;
import com.mulesoft.tools.migration.library.mule.steps.ee.CacheInvalidateKey;
import com.mulesoft.tools.migration.library.mule.steps.ee.CacheObjectStoreCachingStrategy;
import com.mulesoft.tools.migration.library.mule.steps.ee.CacheScope;
import com.mulesoft.tools.migration.library.mule.steps.ee.EETransform;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

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
    return newArrayList(new CompatibilityPomContribution(),
                        new ChoiceExceptionStrategy(),
                        new CatchExceptionStrategy(),
                        new RollbackExceptionStrategy(),
                        new TransactionalScope(),
                        new ExceptionStrategyRef(),
                        new ForEachScope(),
                        new Flow(),
                        new Poll(),
                        new ChoiceExpressions(),
                        new ForEachExpressions(),
                        new SetPayload(),
                        new SetVariable(),
                        new EETransform(),
                        new CacheScope(),
                        new CacheInvalidateKey(),
                        new CacheObjectStoreCachingStrategy(),
                        new CacheHttpCachingStrategy(),
                        new GenericGlobalEndpoint(),
                        new ScatterGather());
  }
}
