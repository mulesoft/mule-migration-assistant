/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine;

import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;
import static com.mulesoft.tools.migration.util.version.VersionUtils.isVersionGreaterOrEquals;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

import com.mulesoft.tools.migration.library.mule.tasks.PreprocessMuleApplication;
import com.mulesoft.tools.migration.library.munit.tasks.MunitMigrationTask;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;
import com.mulesoft.tools.migration.task.MigrationTask;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class MigrationTaskLocatorTest {

  private String from;
  private String to;
  private ProjectType projectType;

  private static String ANY_VERSION_3 = "3.*.*";
  private static String ANY_VERSION_4 = "4.*.*";

  @Before
  public void setUp() {
    from = "3.*.*";
    to = "4.1.1";
    projectType = MULE_FOUR_APPLICATION;
  }

  @Test
  public void locate() {
    MigrationTaskLocator migrationTaskLocator = new MigrationTaskLocator(from, to);
    List<AbstractMigrationTask> migrationTaskList = migrationTaskLocator.locate();

    assertThat("The number of migration task is wrong", migrationTaskList.size(), greaterThan(0));
    MigrationTask migrationTask = migrationTaskList.get(0);
    assertThat("The migration task type is wrong", migrationTask, instanceOf(PreprocessMuleApplication.class));
    assertThat("The migration task from is wrong", isVersionGreaterOrEquals(migrationTask.getFrom(), from), is(true));
    assertThat("The migration task to is wrong", isVersionGreaterOrEquals(to, migrationTask.getTo()), is(true));
    assertThat("The migration task project type is wrong", migrationTask.getProjectType(), is(projectType));
  }

  @Test
  public void locateFromAnyToAny() {
    MigrationTaskLocator migrationTaskLocator = new MigrationTaskLocator(ANY_VERSION_3, ANY_VERSION_4);
    List<AbstractMigrationTask> migrationTaskList = migrationTaskLocator.locate();

    assertThat("The number of migration task is wrong", migrationTaskList.size(), greaterThanOrEqualTo(9));
    MigrationTask migrationTask = migrationTaskList.stream().filter(t -> t instanceof MunitMigrationTask).findFirst().get();

    assertThat("The migration task from is wrong", isVersionGreaterOrEquals(migrationTask.getFrom(), from), is(true));
    assertThat("The migration task to is wrong", isVersionGreaterOrEquals(to, migrationTask.getTo()), is(true));
    assertThat("The migration task project type is wrong", migrationTask.getProjectType(), is(projectType));
  }

}
