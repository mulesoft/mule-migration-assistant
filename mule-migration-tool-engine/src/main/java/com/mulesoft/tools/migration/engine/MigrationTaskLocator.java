/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine;

import com.mulesoft.tools.migration.library.mule.tasks.DbMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.EndpointsMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.FileMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.HTTPCleanupTask;
import com.mulesoft.tools.migration.library.mule.tasks.HTTPMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.MigrationCleanTask;
import com.mulesoft.tools.migration.library.mule.tasks.MuleCoreComponentsMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.MuleDeprecatedCoreComponentsMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.PostprocessMuleApplication;
import com.mulesoft.tools.migration.library.mule.tasks.PreprocessMuleApplication;
import com.mulesoft.tools.migration.library.mule.tasks.PropertiesMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.ScriptingMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.SecurePropertiesMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.SocketsMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.SpringMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.VMMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.ValidationMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.WscMigrationTask;
import com.mulesoft.tools.migration.library.munit.tasks.MunitMigrationTask;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;
import com.mulesoft.tools.migration.task.MigrationTask;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.util.version.VersionUtils.isVersionGreaterOrEquals;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * The goal of this class is to locate migration tasks
 *
 * @author Mulesoft Inc.
 */
public class MigrationTaskLocator {

  private String from;
  private String to;
  private ProjectType projectType;

  public MigrationTaskLocator(String from, String to, ProjectType projectType) {
    checkArgument(from != null, "From must not be null");
    checkArgument(to != null, "To must not be null");
    checkArgument(projectType != null, "ProjectType must not be null");

    this.from = from;
    this.to = to;
    this.projectType = projectType;
  }

  public List<AbstractMigrationTask> locate() {
    List<AbstractMigrationTask> migrationTasks = newArrayList(new PreprocessMuleApplication());
    migrationTasks.addAll(getCoreMigrationTasks());
    migrationTasks.addAll(getMigrationTasks());
    migrationTasks.addAll(getCoreAfterMigrationTasks());
    return migrationTasks.stream().filter(mt -> shouldNotFilterTask(mt)).collect(Collectors.toList());
  }

  protected List<AbstractMigrationTask> getMigrationTasks() {
    ServiceLoader<AbstractMigrationTask> load = ServiceLoader.load(AbstractMigrationTask.class);
    return newArrayList(load);
  }

  private Boolean shouldNotFilterTask(MigrationTask migrationTask) {
    if (!isProperlyCategorized(migrationTask)) {
      // TODO log;
      return FALSE;
    }
    if (projectType.equals(migrationTask.getProjectType())) {
      if (isVersionGreaterOrEquals(migrationTask.getFrom(), from) && isVersionGreaterOrEquals(to, migrationTask.getTo())) {
        return TRUE;
      }
    }
    return FALSE;
  }

  private Boolean isProperlyCategorized(MigrationTask migrationTask) {
    if (migrationTask.getFrom() != null && migrationTask.getTo() != null && migrationTask.getProjectType() != null) {
      return TRUE;
    }
    return FALSE;
  }

  public List<AbstractMigrationTask> getCoreMigrationTasks() {
    List<AbstractMigrationTask> coreMigrationTasks = new ArrayList<>();

    coreMigrationTasks.add(new SecurePropertiesMigrationTask());
    coreMigrationTasks.add(new PropertiesMigrationTask());
    coreMigrationTasks.add(new MuleCoreComponentsMigrationTask());
    coreMigrationTasks.add(new HTTPMigrationTask());
    coreMigrationTasks.add(new SocketsMigrationTask());
    coreMigrationTasks.add(new WscMigrationTask());
    coreMigrationTasks.add(new DbMigrationTask());
    coreMigrationTasks.add(new FileMigrationTask());
    coreMigrationTasks.add(new VMMigrationTask());
    coreMigrationTasks.add(new EndpointsMigrationTask());
    coreMigrationTasks.add(new ScriptingMigrationTask());
    coreMigrationTasks.add(new MuleDeprecatedCoreComponentsMigrationTask());
    coreMigrationTasks.add(new ValidationMigrationTask());
    coreMigrationTasks.add(new MunitMigrationTask());
    // Spring has to run after MUnit, since MUnit in Mule 3 has some custom spring components that are removed by the migrator

    return coreMigrationTasks;
  }

  public List<AbstractMigrationTask> getCoreAfterMigrationTasks() {
    List<AbstractMigrationTask> coreMigrationTasks = new ArrayList<>();

    // Spring has to run after MUnit, since MUnit in Mule 3 has some custom spring components that are removed by the migrator
    coreMigrationTasks.add(new SpringMigrationTask());
    coreMigrationTasks.add(new HTTPCleanupTask());
    coreMigrationTasks.add(new PostprocessMuleApplication());
    coreMigrationTasks.add(new MigrationCleanTask());
    return coreMigrationTasks;
  }
}


