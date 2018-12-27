package com.mulesoft.tools.migration.library.tools.mel;

public class WatermarkSelectorMigrator {

    public String migrateSelector(String expression, String selector) {
// 1 - migrate expression, if returned expression contains mel: display error on report
// 2 - check selector and put max,min or [0] [-1]
// 3 - if selector not match, displar error on report
    }
}
