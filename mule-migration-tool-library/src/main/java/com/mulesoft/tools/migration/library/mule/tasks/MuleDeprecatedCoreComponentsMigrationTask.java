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

import com.mulesoft.tools.migration.library.mule.steps.core.CompatibilityPomContribution;
import com.mulesoft.tools.migration.library.mule.steps.core.CopyAttachments;
import com.mulesoft.tools.migration.library.mule.steps.core.CopyProperties;
import com.mulesoft.tools.migration.library.mule.steps.core.DataMapper;
import com.mulesoft.tools.migration.library.mule.steps.core.JavaReferenceElements;
import com.mulesoft.tools.migration.library.mule.steps.core.MessageAttachmentsListExpressionEvaluator;
import com.mulesoft.tools.migration.library.mule.steps.core.MessagePropertiesTransformer;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveAttachment;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveJsonTransformerNamespace;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveObjectToStringTransformer;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveProperty;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveSchedulersNamespace;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveSessionVariable;
import com.mulesoft.tools.migration.library.mule.steps.core.RemovedElements;
import com.mulesoft.tools.migration.library.mule.steps.core.SetAttachment;
import com.mulesoft.tools.migration.library.mule.steps.core.SetProperty;
import com.mulesoft.tools.migration.library.mule.steps.core.SetSessionVariable;
import com.mulesoft.tools.migration.library.mule.steps.core.component.EchoComponent;
import com.mulesoft.tools.migration.library.mule.steps.core.component.LogComponent;
import com.mulesoft.tools.migration.library.mule.steps.core.component.NullComponent;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.List;

/**
 * Migration definition for deprecated/removed Mule Core components
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MuleDeprecatedCoreComponentsMigrationTask extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Migrate deprecated/removed Mule Core Components";
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
    return newArrayList(new CompatibilityPomContribution(),
                        new EchoComponent(),
                        new LogComponent(),
                        new NullComponent(),
                        new RemovedElements(),
                        new JavaReferenceElements(),
                        new RemoveObjectToStringTransformer(),
                        new SetAttachment(),
                        new SetProperty(),
                        new SetSessionVariable(),
                        new CopyAttachments(),
                        new MessageAttachmentsListExpressionEvaluator(),
                        new CopyProperties(),
                        new RemoveAttachment(),
                        new RemoveProperty(),
                        new RemoveSessionVariable(),
                        new RemoveJsonTransformerNamespace(),
                        new MessagePropertiesTransformer(),
                        new RemoveSchedulersNamespace(),
                        new DataMapper());
  }
}
