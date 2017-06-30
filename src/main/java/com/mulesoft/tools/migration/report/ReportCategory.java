package com.mulesoft.tools.migration.report;

/**
 * Created by davidcisneros on 6/15/17.
 */
public enum ReportCategory {

    RULE_APPLIED("\033[32mRULE APPLIED\033[0m"),
    WORKING_WITH_NODES("\033[36mWORKING WITH NODE\033[0m"),
    ERROR("\033[31mERROR\033[0m"),
    WORKING_WITH_FILE("\033[35mWORKING WITH FILE\033[0m"),
    SKIPPED("\033[33mSKIPPED\033[0m");

    private final String description;

    ReportCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
