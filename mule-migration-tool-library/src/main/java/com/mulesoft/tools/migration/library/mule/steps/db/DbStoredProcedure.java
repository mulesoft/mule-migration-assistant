/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.db;

import static com.mulesoft.tools.migration.library.mule.steps.db.DbConfig.DB_NAMESPACE;
import static com.mulesoft.tools.migration.library.mule.steps.db.DbConfig.DB_NAMESPACE_URI;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateOperationStructure;
import static java.util.stream.Collectors.toList;

import com.mulesoft.tools.migration.library.tools.mel.DefaultMelCompatibilityResolver;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.List;

import org.jdom2.Element;

/**
 * Migrates the call stored procedure operation of the DB Connector
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class DbStoredProcedure extends AbstractDbOperationMigrator {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri() = '" + DB_NAMESPACE_URI + "' and local-name() = 'stored-procedure']";

  @Override
  public String getDescription() {
    return "Update call stored procedure operation of the DB Connector.";
  }

  public DbStoredProcedure() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    migrateSql(object);
    migrateInputParamTypes(object);
    migrateInputParams(object);

    List<Element> outParams = object.getChildren("out-param", DB_NAMESPACE).stream()
        .map(ip -> new Element("output-parameter", DB_NAMESPACE)
            .setAttribute("key", ip.getAttributeValue("name")))
        .collect(toList());
    if (!outParams.isEmpty()) {
      object.addContent(new Element("output-parameters", DB_NAMESPACE).addContent(outParams));
    }
    object.removeChildren("out-param", DB_NAMESPACE);

    List<Element> inoutParams = object.getChildren("inout-param", DB_NAMESPACE).stream()
        .map(ip -> new Element("in-out-parameter", DB_NAMESPACE)
            .setAttribute("key", ip.getAttributeValue("name"))
            .setAttribute("value", ip.getAttributeValue("value")))
        .collect(toList());
    if (!inoutParams.isEmpty()) {
      object.addContent(new Element("in-out-parameters", DB_NAMESPACE).addContent(inoutParams));
    }
    object.removeChildren("inout-param", DB_NAMESPACE);

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
