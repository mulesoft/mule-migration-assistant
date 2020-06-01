/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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
   * @param cause the exception to thrown
   */
  public MigrationStepException(String message, Throwable cause) {
    super(message, cause);
  }

}
