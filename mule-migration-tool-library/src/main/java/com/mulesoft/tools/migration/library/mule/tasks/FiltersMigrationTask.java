/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.tasks;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

import com.mulesoft.tools.migration.library.mule.steps.core.filter.AndFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.CustomFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.ExpressionFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.FilterReference;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.IdempotentMessageFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.IdempotentSecureHashMessageFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.MessageFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.MessageFilterReference;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.MessagePropertyFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.NotFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.OrFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.PayloadTypeFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.RegexFilter;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.WildcardFilter;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.List;

/**
 * Migration definition for filters
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class FiltersMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Migrate Filters to validations";
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
  public List<MigrationStep> getSteps() {
    return newArrayList(new FilterReference(),
                        new MessageFilterReference(),
                        new MessageFilter(),
                        new CustomFilter(),
                        new ExpressionFilter(),
                        new RegexFilter(),
                        new WildcardFilter(),
                        new PayloadTypeFilter(),
                        new MessagePropertyFilter(),
                        new AndFilter(),
                        new OrFilter(),
                        new NotFilter(),

                        new IdempotentMessageFilter(),
                        new IdempotentSecureHashMessageFilter());
  }
}
