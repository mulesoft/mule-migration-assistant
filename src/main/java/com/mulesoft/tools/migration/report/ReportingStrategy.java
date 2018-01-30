package com.mulesoft.tools.migration.report;

import com.mulesoft.tools.migration.task.MigrationTask;
import com.mulesoft.tools.migration.task.step.MigrationStep;

/**
 * Created by davidcisneros on 6/7/17.
 */
public interface ReportingStrategy {

    void log(String message, ReportCategory reportCategory, String filePath, MigrationTask task, MigrationStep step);
}
