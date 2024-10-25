/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
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
