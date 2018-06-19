/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.db;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateOperationStructure;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateOperationStructure;

import com.mulesoft.tools.migration.library.tools.mel.DefaultMelCompatibilityResolver;
import com.mulesoft.tools.migration.library.tools.mel.HeaderSyntaxCompatibilityResolver;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import com.mulesoft.tools.migration.step.util.XmlDslUtils;
import org.jdom2.Element;

/**
 * Migrates the execute-ddl operation of the DB Connector
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class DbDdlExecute extends AbstractDbOperationMigrator {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri() = '" + DB_NAMESPACE_URI + "' and local-name() = 'execute-ddl']";

  @Override
  public String getDescription() {
    return "Update execute-ddl operation of the DB Connector.";
  }

  public DbDdlExecute() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    migrateSql(object);
    if (object.getAttribute("source") != null) {
      report.report(ERROR, object, object, "'source' attribute does not exist in Mule 4. Update the query accordingly.",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-database#database_dynamic_queries");
      object.removeAttribute("source");
    }

    migrateOperationStructure(getApplicationModel(), object, report, false, getExpressionMigrator(),
                              new DefaultMelCompatibilityResolver());
  }


}
