/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration;

import com.mulesoft.tools.migration.step.category.MigrationReport;

/**
 * An interface to handle the task execution
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public interface Executable {

  void execute(MigrationReport report) throws Exception;
}
