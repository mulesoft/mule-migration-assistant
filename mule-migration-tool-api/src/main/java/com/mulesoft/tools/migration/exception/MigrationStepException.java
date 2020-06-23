/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.exception;


import com.mulesoft.tools.migration.step.MigrationStep;

/**
 * Signals an issue in a {@link MigrationStep}
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MigrationStepException extends RuntimeException {

  /**
   * Create a new migration exception
   *
   * @param message the message to display on exception
   */
  public MigrationStepException(String message) {
    super(message);
  }

  /**
   * Create a new migration exception
   *
   * @param message the message to display on exception
   * @param cause the exception to be thrown
   */
  public MigrationStepException(String message, Throwable cause) {
    super(message, cause);
  }

}
