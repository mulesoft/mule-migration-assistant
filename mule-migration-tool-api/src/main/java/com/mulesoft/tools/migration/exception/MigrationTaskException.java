/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.exception;

import com.mulesoft.tools.migration.task.AbstractMigrationTask;

/**
 * Signals an issue in a {@link AbstractMigrationTask}
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MigrationTaskException extends Exception {

  /**
   * Create a new migration exception
   *
   * @param message the message to display on exception
   */
  public MigrationTaskException(String message) {
    super(message);
  }

  /**
   * Create a new migration exception
   *
   * @param message the message to display on exception
   * @param exception the exception to be thrown
   */
  public MigrationTaskException(String message, Exception exception) {
    super(message, exception);
  }
}
