/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.exception;

/**
 * Signals an issue with the options provided by the console
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ConsoleOptionsException extends Exception {

  public ConsoleOptionsException(String message) {
    super(message);
  }
}
