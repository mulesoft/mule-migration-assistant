/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.exception;

/**
 * Signals a failures in a {@link com.mulesoft.tools.migration.MigrationJob}
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MigrationJobException extends Exception {

  // private String filePath;
  // private List<String> exceptions;

  public MigrationJobException(String message) {
    // , List<String> exceptions, String filePath) {
    super(message);
    // this.filePath = filePath;
    // this.exceptions = exceptions;
  }



}
