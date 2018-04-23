/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.munit.tasks;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;

import com.mulesoft.tools.migration.library.munit.steps.AssertEquals;
import com.mulesoft.tools.migration.library.munit.steps.AssertFalse;
import com.mulesoft.tools.migration.library.munit.steps.AssertNotEquals;
import com.mulesoft.tools.migration.library.munit.steps.AssertNotNullPayload;
import com.mulesoft.tools.migration.library.munit.steps.AssertNullPayload;
import com.mulesoft.tools.migration.library.munit.steps.AssertPayload;
import com.mulesoft.tools.migration.library.munit.steps.AssertTrue;
import com.mulesoft.tools.migration.library.munit.steps.MUnitNamespaces;
import com.mulesoft.tools.migration.library.munit.steps.MUnitPomContribution;
import com.mulesoft.tools.migration.library.munit.steps.Mock;
import com.mulesoft.tools.migration.library.munit.steps.MoveMUnitProcessorsToSections;
import com.mulesoft.tools.migration.library.munit.steps.RemoveSpringImport;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;
import com.mulesoft.tools.migration.task.Version;
import com.mulesoft.tools.migration.task.Version.VersionBuilder;

import java.util.List;

/**
 * Migration Task for MUnit components
 *
 * @author Mulesoft Inc.
 */
public class MunitMigrationTask extends AbstractMigrationTask {

  @Override
  public Version getFrom() {
    return new VersionBuilder().withMajor("3").withMinor("8").withRevision("0").build();
  }

  @Override
  public Version getTo() {
    return new VersionBuilder().withMajor("4").withMinor("0").withRevision("0").build();
  }

  @Override
  public ProjectType getProjectType() {
    return MULE_FOUR_APPLICATION;
  }


  @Override
  public List<MigrationStep> getSteps() {
    return newArrayList(new AssertEquals(), new AssertNotEquals(), new AssertNotNullPayload(), new AssertNullPayload(),
                        new AssertPayload(), new AssertTrue(), new AssertFalse(), new Mock(), new MUnitNamespaces(),
                        new MoveMUnitProcessorsToSections(), new MUnitPomContribution(), new RemoveSpringImport());
  }

  @Override
  public String getDescription() {
    return null;
  }

}
