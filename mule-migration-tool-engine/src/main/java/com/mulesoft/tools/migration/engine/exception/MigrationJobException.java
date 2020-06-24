/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.engine.exception;

import com.mulesoft.tools.migration.engine.MigrationJob;

/**
 * Signals a failures in a {@link MigrationJob}
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MigrationJobException extends Exception {


  public MigrationJobException(String message) {
    super(message);
  }

  public MigrationJobException(String message, Throwable cause) {
    super(String.format("$s %n %s", message, cause.getStackTrace()), cause);
  }
}
