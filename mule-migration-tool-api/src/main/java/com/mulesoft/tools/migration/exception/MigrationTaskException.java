/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
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
