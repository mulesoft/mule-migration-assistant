/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.step;

import com.mulesoft.tools.migration.step.category.MigrationReport;

/**
 * An interface to handle the step execution
 * @param <T>
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public interface StepExecutable<T> {

  void execute(T object, MigrationReport report) throws RuntimeException;

}
