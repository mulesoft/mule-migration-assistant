/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine.task;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.mulesoft.tools.migration.project.structure.ProjectType;

/**
 * The goal of this class is to locate migration tasks
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

  public List<MigrationTask> locate() {
    List<MigrationTask> migrationTasks = getMigrationTasks();

    return migrationTasks.stream().filter(mt -> shouldNotFilterTask(mt)).collect(Collectors.toList());
  }

  protected List<MigrationTask> getMigrationTasks() {
    ServiceLoader<MigrationTask> load = ServiceLoader.load(MigrationTask.class);
    return Lists.newArrayList(load);
  }

  private Boolean shouldNotFilterTask(MigrationTask migrationTask) {
    if (!isProperlyCategorized(migrationTask)) {
      // TODO log;
      return FALSE;
    }

    if (projectType.equals(migrationTask.getProjectType())) {
      if (from.equals(migrationTask.getFrom()) && to.equals(migrationTask.getTo())) {
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
}


