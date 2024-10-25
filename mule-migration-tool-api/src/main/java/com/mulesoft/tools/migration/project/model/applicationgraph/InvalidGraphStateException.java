/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

/**
 * Exception generated when the graph reaches an inconsistent state
 *
 * @author Mulesoft Inc.
 */
public class InvalidGraphStateException extends RuntimeException {

  public InvalidGraphStateException(String message) {
    super(message);
  }
}
