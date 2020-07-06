package com.mulesoft.tools.migration.library.mule.tasks;

import com.mulesoft.tools.migration.library.mule.steps.salesforce.CreateOperation;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;

public class SalesforceMigrationTask extends AbstractMigrationTask {
    @Override
    public String getDescription() {
        return "Migrate Salesforce connector operations/configurations";
    }

    @Override
    public String getTo() {
        return MULE_4_VERSION;
    }

    @Override
    public String getFrom() {
        return MULE_3_VERSION;
    }

    @Override
    public List<MigrationStep> getSteps() {
        return newArrayList(new CreateOperation());
    }
}
