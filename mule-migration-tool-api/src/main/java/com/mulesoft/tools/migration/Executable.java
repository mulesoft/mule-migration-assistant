/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine;

/**
 * An interface to handle the task execution
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public interface Executable {

  void execute() throws Exception;
}
