/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.exception;

/**
 * Signals an issue in a {@link com.mulesoft.tools.migration.task.step.MigrationStep}
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MigrationStepException extends Exception {

  public MigrationStepException(String message) {
    super(message);
  }

}
