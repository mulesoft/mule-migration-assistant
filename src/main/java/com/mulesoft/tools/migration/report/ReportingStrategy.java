/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.report;

import com.mulesoft.tools.migration.task.MigrationTask;
import com.mulesoft.tools.migration.task.step.MigrationStep;

/**
 * Created by davidcisneros on 6/7/17.
 */
public interface ReportingStrategy {

    void log(String message, ReportCategory reportCategory, String filePath, MigrationTask task, MigrationStep step);
}
