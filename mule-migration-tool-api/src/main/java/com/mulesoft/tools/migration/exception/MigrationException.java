/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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
