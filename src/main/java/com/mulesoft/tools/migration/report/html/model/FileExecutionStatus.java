/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.report.html.model;

import java.util.ArrayList;

/**
 * Stores data of a migrated file
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class FileExecutionStatus {

  private String fileName;
  private ArrayList<TaskExecutionStatus> tasksApplied = new ArrayList<>();

  public FileExecutionStatus(String fileName) {
    this.setFileName(fileName);
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public void addTaskApplied(TaskExecutionStatus taskApplied) {
    tasksApplied.add(taskApplied);
  }
}
