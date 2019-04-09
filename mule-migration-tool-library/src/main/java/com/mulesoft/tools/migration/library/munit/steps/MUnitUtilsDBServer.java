/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.munit.steps;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * This steps migrates the MUnit Utils DB Server
 * @author Mulesoft Inc.
 */
public class MUnitUtilsDBServer extends AbstractApplicationModelMigrationStep {

  private static final String DB_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/dbserver";
  private static final String DB_NAMESPACE_PREFIX = "dbserver";
  private static final Namespace DB_NAMESPACE = Namespace.getNamespace(DB_NAMESPACE_PREFIX, DB_NAMESPACE_URI);

  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + DB_NAMESPACE_URI + "']";

  @Override
  public String getDescription() {
    return "Migrate MUnit DB Utils processor.";
  }

  public MUnitUtilsDBServer() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(DB_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {

    if (element.getName().equals("start-db-server") || element.getName().equals("stop-db-server")) {
      report.report("munit.dbServer", element, element.getParentElement());
      element.detach();
    } else if (element.getName().equals("config")) {

      Element connection = new Element("connection", DB_NAMESPACE);
      copyAttributeIfPresent(element, connection, "csv");
      copyAttributeIfPresent(element, connection, "database");
      copyAttributeIfPresent(element, connection, "sqlFile");
      copyAttributeIfPresent(element, connection, "connectionStringParameters");

      element.addContent(connection);
    }
  }
}

