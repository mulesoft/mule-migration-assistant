/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine;

import com.mulesoft.tools.migration.library.mule.tasks.PreprocessMuleApplication;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;
import com.mulesoft.tools.migration.task.MigrationTask;
import com.mulesoft.tools.migration.task.Version;
import com.mulesoft.tools.migration.task.Version.VersionBuilder;
import com.mulesoft.tools.migration.library.munit.tasks.MunitMigrationTask;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;
import static com.mulesoft.tools.migration.task.Version.VersionBuilder.ANY_VERSION;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MigrationTaskLocatorTest {

  private Version from;
  private Version to;
  private ProjectType projectType;

  @Before
  public void setUp() {
    from = new VersionBuilder().withMajor("3").withMinor("8").withRevision("0").build();
    to = new VersionBuilder().withMajor("4").withMinor("0").withRevision("0").build();
    projectType = MULE_FOUR_APPLICATION;
  }

  @Test
  public void locate() {
    MigrationTaskLocator migrationTaskLocator = new MigrationTaskLocator(from, to, projectType);
    List<AbstractMigrationTask> migrationTaskList = migrationTaskLocator.locate();

    assertThat("The number of migration task is wrong", migrationTaskList.size(), is(1));
    MigrationTask migrationTask = migrationTaskList.get(0);
    assertThat("The migration task type is wrong", migrationTask, instanceOf(MunitMigrationTask.class));
    assertThat("The migration task from is wrong", migrationTask.getFrom(), is(from));
    assertThat("The migration task to is wrong", migrationTask.getTo(), is(to));
    assertThat("The migration task project type is wrong", migrationTask.getProjectType(), is(projectType));
  }

  @Test
  public void locateFromAnyToAny() {
    MigrationTaskLocator migrationTaskLocator = new MigrationTaskLocator(ANY_VERSION, ANY_VERSION, projectType);
    List<AbstractMigrationTask> migrationTaskList = migrationTaskLocator.locate();

    assertThat("The number of migration task is wrong", migrationTaskList.size(), is(5));
    MigrationTask migrationTask = migrationTaskList.get(4);
    assertThat("The migration task type is wrong", migrationTask, instanceOf(MunitMigrationTask.class));
    assertThat("The migration task from is wrong", migrationTask.getFrom(), is(from));
    assertThat("The migration task to is wrong", migrationTask.getTo(), is(to));
    assertThat("The migration task project type is wrong", migrationTask.getProjectType(), is(projectType));
  }

}
