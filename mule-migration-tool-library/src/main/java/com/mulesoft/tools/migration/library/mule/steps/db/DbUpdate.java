/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.db;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateEnrichers;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateOperationStructure;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.mulesoft.tools.migration.library.tools.mel.EnrichersCompatibilityResolver;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Migrates the update operation of the DB Connector
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class DbUpdate extends AbstractDbOperationMigrator {

  public static final String XPATH_SELECTOR = "//*[namespace-uri() = '" + DB_NAMESPACE_URI + "' and local-name() = 'update']";

  @Override
  public String getDescription() {
    return "Update update operation of the DB Connector.";
  }

  public DbUpdate() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    migrateSql(object);

    if ("true".equals(object.getAttributeValue("bulkMode"))) {
      object.setName("bulk-update");
      object.removeAttribute("bulkMode");
      migrateEnrichers(object, getExpressionMigrator(), new EnrichersCompatibilityResolver(), getApplicationModel());
      migrateBulkInputParams(object);
    } else {
      migrateInputParams(object);
    }

    if (object.getAttribute("source") != null) {
      report.report(ERROR, object, object, "'source' attribute does not exist in Mule 4. Update the query accordingly.",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-database#database_dynamic_queries");
      object.removeAttribute("source");
    }

    migrateOperationStructure(getApplicationModel(), object, report, false);
  }


}
