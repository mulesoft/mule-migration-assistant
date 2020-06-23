/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.exception;

/**
 * Will cancel the currently running migration and force it to fail
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MigrationAbortException extends RuntimeException {

  /**
   * Create a new migration exception
   *
   * @param message the message to display on exception
   */
  public MigrationAbortException(String message) {
    super(message);
  }

  /**
   * Create a new migration exception
   *
   * @param message the message to display on exception
   * @param exception the exception to be thrown
   */
  public MigrationAbortException(String message, Exception exception) {
    super(message, exception);
  }
}
