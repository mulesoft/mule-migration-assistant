/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.munit.tasks;

import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;
import com.mulesoft.tools.migration.task.Version;
import com.mulesoft.tools.migration.task.Version.VersionBuilder;
import com.mulesoft.tools.migration.library.munit.steps.*;

import java.util.Set;

import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;
import static com.google.common.collect.Sets.newHashSet;

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
  public Set<MigrationStep> getSteps() {
    return newHashSet(new AssertEquals(), new AssertNotEquals(), new AssertNotNullPayload(), new AssertNullPayload(),
                      new AssertPayload(), new AssertTrue(), new AssertFalse(), new Mock(), new MUnitNamespaces(),
                      new MoveMUnitProcessorsToSections(), new MUnitPomContribution());
  }

  @Override
  public String getDescription() {
    return null;
  }

}
