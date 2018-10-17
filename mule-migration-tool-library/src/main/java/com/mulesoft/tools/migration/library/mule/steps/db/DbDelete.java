/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.db;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateOperationStructure;
import static java.util.stream.Collectors.toList;

import com.mulesoft.tools.migration.library.tools.mel.DefaultMelCompatibilityResolver;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

import java.util.List;

/**
 * Migrates the delete operation of the DB Connector
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class DbDelete extends AbstractDbOperationMigrator {

  public static final String XPATH_SELECTOR = "//*[namespace-uri() = '" + DB_NAMESPACE_URI + "' and local-name() = 'delete']";

  @Override
  public String getDescription() {
    return "Update delete operation of the DB Connector.";
  }

  public DbDelete() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    migrateSql(object);
    if ("true".equals(object.getAttributeValue("bulkMode"))) {
      object.setName("bulk-delete");
      object.removeAttribute("bulkMode");
      migrateBulkInputParams(object);
    } else {
      List<Element> paramTypes = object.getChildren("in-param", DB_NAMESPACE).stream()
          .filter(ip -> ip.getAttribute("type") != null)
          .map(ip -> new Element("parameter-type", DB_NAMESPACE)
              .setAttribute("key", ip.getAttributeValue("name"))
              .setAttribute("type", ip.getAttributeValue("type")))
          .collect(toList());
      if (!paramTypes.isEmpty()) {
        object.addContent(new Element("parameter-types", DB_NAMESPACE).addContent(paramTypes));
      }

      migrateInputParams(object);
    }

    if (object.getAttribute("source") != null) {
      report.report("db.source", object, object);
      object.removeAttribute("source");
    }

    migrateOperationStructure(getApplicationModel(), object, report, false, getExpressionMigrator(),
                              new DefaultMelCompatibilityResolver());
  }


}
