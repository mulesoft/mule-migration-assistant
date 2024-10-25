/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.exception;

/**
 * Signals any issue during the migration execution
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MigrationException extends Exception {

  /**
   * Create a new migration exception
   *
   * @param message to display on exception
   */
  public MigrationException(String message) {
    super(message);
  }

  /**
   * Create a new migration exception
   *
   * @param message the message to display on exception
   * @param cause the exception to be thrown
   */
  public MigrationException(String message, Throwable cause) {
    super(String.format("$s %n %s", message, cause.getStackTrace()), cause);
  }
}
