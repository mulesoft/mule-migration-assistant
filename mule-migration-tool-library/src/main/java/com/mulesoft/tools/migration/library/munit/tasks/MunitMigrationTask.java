/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.munit.tasks;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

import com.mulesoft.tools.migration.library.munit.steps.AssertEquals;
import com.mulesoft.tools.migration.library.munit.steps.AssertFalse;
import com.mulesoft.tools.migration.library.munit.steps.AssertNotEquals;
import com.mulesoft.tools.migration.library.munit.steps.AssertNotNullPayload;
import com.mulesoft.tools.migration.library.munit.steps.AssertNullPayload;
import com.mulesoft.tools.migration.library.munit.steps.AssertPayload;
import com.mulesoft.tools.migration.library.munit.steps.AssertTrue;
import com.mulesoft.tools.migration.library.munit.steps.MUnitConfig;
import com.mulesoft.tools.migration.library.munit.steps.MUnitNamespaces;
import com.mulesoft.tools.migration.library.munit.steps.MUnitPomContribution;
import com.mulesoft.tools.migration.library.munit.steps.MUnitTest;
import com.mulesoft.tools.migration.library.munit.steps.Mock;
import com.mulesoft.tools.migration.library.munit.steps.RemoveImport;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.List;

/**
 * Migration Task for MUnit components
 *
 * @author Mulesoft Inc.
 */
public class MunitMigrationTask extends AbstractMigrationTask {

  @Override
  public String getFrom() {
    return MULE_3_VERSION;
  }

  @Override
  public String getTo() {
    return MULE_4_VERSION;
  }

  @Override
  public ProjectType getProjectType() {
    return MULE_FOUR_APPLICATION;
  }

  @Override
  public List<MigrationStep> getSteps() {
    return newArrayList(new AssertEquals(), new AssertNotEquals(), new AssertNotNullPayload(), new AssertNullPayload(),
                        new AssertPayload(), new AssertTrue(), new AssertFalse(), new Mock(), new MUnitNamespaces(),
                        new MUnitTest(), new MUnitPomContribution(), new RemoveImport(), new MUnitConfig());
  }

  @Override
  public String getDescription() {
    return null;
  }

}
