/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.mulesoft.tools.migration.library.mule.tasks.EndpointsMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.FileMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.HTTPMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.MuleCoreComponentsMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.PostprocessMuleApplication;
import com.mulesoft.tools.migration.library.mule.tasks.PreprocessMuleApplication;
import com.mulesoft.tools.migration.library.mule.tasks.ScriptingMigrationTask;
import com.mulesoft.tools.migration.library.mule.tasks.SocketsMigrationTask;
import com.mulesoft.tools.migration.library.munit.tasks.MunitMigrationTask;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;
import com.mulesoft.tools.migration.task.MigrationTask;
import com.mulesoft.tools.migration.task.Version;

/**
 * The goal of this class is to locate migration tasks
 *
 * @author Mulesoft Inc.
 */
public class MigrationTaskLocator {

  private Version from;
  private Version to;
  private ProjectType projectType;

  public MigrationTaskLocator(Version from, Version to, ProjectType projectType) {
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
    migrationTasks.add(new PostprocessMuleApplication());
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
      if (from.matches(migrationTask.getFrom()) && to.matches(migrationTask.getTo())) {
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

    coreMigrationTasks.add(new MuleCoreComponentsMigrationTask());
    coreMigrationTasks.add(new HTTPMigrationTask());
    coreMigrationTasks.add(new SocketsMigrationTask());
    coreMigrationTasks.add(new FileMigrationTask());
    coreMigrationTasks.add(new EndpointsMigrationTask());
    coreMigrationTasks.add(new ScriptingMigrationTask());
    coreMigrationTasks.add(new MunitMigrationTask());

    return coreMigrationTasks;
  }
}


