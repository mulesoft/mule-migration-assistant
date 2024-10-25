/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.step.category;

/**
 * Indicates the migration result, either success or failure for all the instances of a given component.
 *
 * @author Mulesoft Inc.
 */
public class ComponentMigrationStatus {

  private int success;
  private int failure;

  public int getSuccess() {
    return success;
  }

  public int getFailure() {
    return failure;
  }

  public void success() {
    success++;
  }

  public void failure() {
    failure++;
  }
}
