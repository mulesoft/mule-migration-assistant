package com.mulesoft.tools.migration.report.console;

import com.mulesoft.tools.migration.report.ReportCategory;
import com.mulesoft.tools.migration.report.ReportingStrategy;
import com.mulesoft.tools.migration.task.MigrationTask;
import com.mulesoft.tools.migration.task.step.MigrationStep;

/**
 * Created by davidcisneros on 6/7/17.
 */
public class ConsoleReportStrategy implements ReportingStrategy {

    @Override
    public void log(String message, ReportCategory reportCategory, String filePath, MigrationTask task, MigrationStep step) {
        System.out.println(reportCategory.getDescription() + " : " + message);
    }
}
