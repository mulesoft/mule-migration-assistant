/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.step;

import com.mulesoft.tools.migration.step.category.MigrationReport;

/**
 * An interface to handle the step execution.
 * @param <T>
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public interface StepExecutable<T> {

  void execute(T object, MigrationReport report) throws RuntimeException;

}
