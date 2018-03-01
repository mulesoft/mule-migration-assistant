/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;

import org.jdom2.Document;

import com.mulesoft.tools.migration.engine.exception.MigrationTaskException;
import com.mulesoft.tools.migration.project.model.ApplicationModel;

/**
 * A task is composed by one or more steps
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MigrationTask implements Executable {

  private String description;
  private Boolean onErrorStop;

  private ApplicationModel applicationModel;
  private ArrayList<MigrationStep> migrationSteps;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setOnErrorStop(Boolean onErrorStop) {
    this.onErrorStop = onErrorStop;
  }

  public void setApplicationModel(ApplicationModel applicationModel) {
    checkArgument(applicationModel != null, "The application model must not be null.");
    this.applicationModel = applicationModel;
  }

  public void setMigrationSteps(ArrayList<MigrationStep> migrationSteps) {
    checkArgument(migrationSteps != null, "The migration steps must not be null.");

    this.migrationSteps = migrationSteps;
  }

  public void execute() throws Exception {
    checkState(applicationModel != null, "An application model must be provided.");
    try {
      for (MigrationStep step : migrationSteps) {
        step.setApplicationModel(applicationModel);
        step.execute();
      }
    } catch (Exception ex) {
      if (onErrorStop) {
        // TODO report this failure properly
        throw new MigrationTaskException("Task execution exception. " + ex.getMessage());
      }
    }
  }

  @Deprecated
  public void setDocument(Document document) {
    // this.doc = document;
  }

  @Deprecated
  public MigrationTask(String xpathSelector) {
    // this.xpathSelector = xpathSelector;
    this.migrationSteps = new ArrayList<>();
  }

  @Deprecated
  public MigrationTask() {
    this.migrationSteps = new ArrayList<>();
  }

  @Deprecated
  public void addStep(MigrationStep step) {
    if (step != null) {
      this.migrationSteps.add(step);
    }
  }
}
