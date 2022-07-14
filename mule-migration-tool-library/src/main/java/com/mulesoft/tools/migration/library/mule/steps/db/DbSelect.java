/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.db;

import static com.mulesoft.tools.migration.library.mule.steps.db.DbConfig.DB_NAMESPACE_URI;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateOperationStructure;

import com.mulesoft.tools.migration.library.tools.mel.DefaultMelCompatibilityResolver;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrates the select operation of the DB Connector
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class DbSelect extends AbstractDbOperationMigrator {

  public static final String XPATH_SELECTOR = "//*[namespace-uri() = '" + DB_NAMESPACE_URI + "' and local-name() = 'select']";

  @Override
  public String getDescription() {
    return "Update select operation of the DB Connector.";
  }

  public DbSelect() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    migrateSql(object);
    migrateInputParams(object);

    if (object.getAttribute("streaming") == null || "false".equals(object.getAttributeValue("streaming"))) {
      report.report("db.streaming", object, object);
    }
    object.removeAttribute("streaming");

    if (object.getAttribute("source") != null) {
      report.report("db.source", object, object);
      object.removeAttribute("source");
    }

    migrateOperationStructure(getApplicationModel(), object, report, false, getExpressionMigrator(),
                              new DefaultMelCompatibilityResolver());
  }

}
