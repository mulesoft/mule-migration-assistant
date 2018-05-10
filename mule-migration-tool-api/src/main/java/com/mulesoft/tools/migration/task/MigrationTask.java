/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.task;

import com.mulesoft.tools.migration.Executable;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.MigrationStep;

import java.util.List;

/**
 * It is a container of {@link MigrationStep} that can be categorized
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public interface MigrationTask extends Executable, Categorizable {

  /**
   * Retrieves the task description. It should describe briefly what the task objective.
   *
   * @return a {@link String}
   */
  String getDescription();

  /**
   * Retrieves the steps that compose the task.
   *
   * @return a {@link List<MigrationStep>}
   */
  List<MigrationStep> getSteps();

  /**
   * Retrieves the {@link ApplicationModel} that represents the migration current state.
   *
   * @return an {@link ApplicationModel}
   */
  ApplicationModel getApplicationModel();

  /**
   * Sets the {@link ApplicationModel} on which the task is going to work over.
   *
   * @return an {@link ApplicationModel}
   */
  void setApplicationModel(ApplicationModel applicationModel);

}
