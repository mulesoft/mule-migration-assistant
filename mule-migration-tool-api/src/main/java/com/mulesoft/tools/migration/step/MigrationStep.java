/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.step;

/**
 * Building block of the execution engine.
 * @param <T>
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public interface MigrationStep<T> extends StepExecutable<T> {

  String getDescription();
}
