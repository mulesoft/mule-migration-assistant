/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.step.other;

import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.ArrayList;
import java.util.List;

/**
 * JUST A CONCEPT
 * todo delete
 * @author Mulesoft Inc.
 */
public class AnotherMigrationTask extends AbstractMigrationTask {

  @Override
  public String getTo() {
    return null;
  }

  @Override
  public String getFrom() {
    return null;
  }

  @Override
  public List<MigrationStep> getSteps() {
    List<MigrationStep> steps = new ArrayList<>();

    return steps;
  }

  @Override
  public String getDescription() {
    return null;
  }
}
