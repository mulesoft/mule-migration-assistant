/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.db;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateReconnection;
import static java.util.stream.Collectors.toList;
import static org.jdom2.Content.CType.Element;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Migrates the config elements of the DB Connector
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class DbConfig extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  private static final String DB_NAMESPACE_PREFIX = "db";
  public static final String DB_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/db";
  public static final Namespace DB_NAMESPACE = Namespace.getNamespace(DB_NAMESPACE_PREFIX, DB_NAMESPACE_URI);

  public static final String XPATH_SELECTOR = "/*/*[namespace-uri() = '" + DB_NAMESPACE_URI + "']";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update config elements of the DB Connector.";
  }

  public DbConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(DB_NAMESPACE));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {

    if ("template-query".equals(object.getName())) {
      List<Element> templateRefs = getApplicationModel().getNodes("//*[namespace-uri() = '" + DB_NAMESPACE_URI
          + "' and local-name() = 'template-query-ref' and @name = '" + object.getAttributeValue("name") + "']");

      for (Element templateRef : new ArrayList<>(templateRefs)) {
        List<Content> migratedChildren = object.cloneContent();
        for (Content migratedChild : migratedChildren) {
          if (Element == migratedChild.getCType() && "in-param".equals(((Element) migratedChild).getName())) {
            Element migratedChildElement = (Element) migratedChild;
            if (migratedChildElement.getAttribute("defaultValue") != null) {
              migratedChildElement.getAttribute("defaultValue").setName("value");
            }
          }
        }
        templateRef.getParent().addContent(templateRef.getParent().indexOf(templateRef), migratedChildren);
        templateRef.detach();
      }

      object.detach();

      return;
    }

    Element dataTypes = object.getChild("data-types", DB_NAMESPACE);
    if (dataTypes != null) {
      dataTypes.setName("column-types");
      for (Element dataType : dataTypes.getChildren("data-type", DB_NAMESPACE)) {
        dataType.setName("column-type");
        dataType.getAttribute("name").setName("typeName");
      }
    }

    Element connection = null;
    if (object.getAttribute("dataSource-ref") != null) {
      report.report("db.referencedDataSource", object, object, object.getName());

      connection = new Element("data-source-connection", DB_NAMESPACE);
      object.addContent(connection);
      copyAttributeIfPresent(object, connection, "dataSource-ref", "dataSourceRef");

      List<Attribute> otherAttributes =
          object.getAttributes().stream().filter(att -> !"name".equals(att.getName())).collect(toList());
      if (!otherAttributes.isEmpty()) {
        report.report("db.configAttributesOverlap", connection, connection, otherAttributes.toString());
      }
    } else if (object.getAttribute("url") != null) {
      connection = new Element("generic-connection", DB_NAMESPACE);
      object.addContent(connection);

      copyAttributeIfPresent(object, connection, "user");
      copyAttributeIfPresent(object, connection, "password");
      copyAttributeIfPresent(object, connection, "url");
      copyAttributeIfPresent(object, connection, "useXaTransactions");
      copyAttributeIfPresent(object, connection, "transactionIsolation");

      if (!copyAttributeIfPresent(object, connection, "driverClassName")) {
        if ("derby-config".equals(object.getName())) {
          connection.setAttribute("driverClassName", "org.apache.derby.jdbc.EmbeddedDriver");
        } else if ("mysql-config".equals(object.getName())) {
          connection.setAttribute("driverClassName", "com.mysql.jdbc.Driver");

          report.report("db.jdbcDriverDependency", connection, connection);
        } else if ("oracle-config".equals(object.getName())) {
          connection.setAttribute("driverClassName", "oracle.jdbc.driver.OracleDriver");

          report.report("db.jdbcDriverDependency", connection, connection);
        } else {
          report.report("db.jdbcDriverDependency", connection, connection);

        }
      }

      report.report("db.jdbcUrlForSpecificEngine", connection, connection);

      Element connectionProps = object.getChild("connection-properties", DB_NAMESPACE);
      if (connectionProps != null) {
        // Have to use isPresent() because connection cannot be final
        Optional<Element> userProp = connectionProps.getChildren("property", DB_NAMESPACE)
            .stream()
            .filter(p -> "user".equals(p.getAttributeValue("key")))
            .findFirst();
        if (userProp.isPresent()) {
          connection.setAttribute("user", userProp.get().getAttributeValue("value"));
          connectionProps.removeContent(userProp.get());
        }

        if (connectionProps.getChildren().isEmpty()) {
          object.removeContent(connectionProps);
        }
      }
    } else if ("derby-config".equals(object.getName())) {
      connection = new Element("derby-connection", DB_NAMESPACE);
      object.addContent(connection);
      copyAttributeIfPresent(object, connection, "user");
      copyAttributeIfPresent(object, connection, "password");
      copyAttributeIfPresent(object, connection, "useXaTransactions");
      copyAttributeIfPresent(object, connection, "transactionIsolation");
    } else if ("mysql-config".equals(object.getName())) {
      connection = new Element("my-sql-connection", DB_NAMESPACE);
      object.addContent(connection);
      copyAttributeIfPresent(object, connection, "database");
      copyAttributeIfPresent(object, connection, "host");
      copyAttributeIfPresent(object, connection, "port");
      copyAttributeIfPresent(object, connection, "user");
      copyAttributeIfPresent(object, connection, "password");
      copyAttributeIfPresent(object, connection, "useXaTransactions");
      copyAttributeIfPresent(object, connection, "transactionIsolation");

      report.report("db.jdbcDriverDependency", connection, connection);
    } else if ("oracle-config".equals(object.getName())) {
      connection = new Element("oracle-connection", DB_NAMESPACE);
      object.addContent(connection);
      copyAttributeIfPresent(object, connection, "host");
      copyAttributeIfPresent(object, connection, "port");
      copyAttributeIfPresent(object, connection, "instance");
      copyAttributeIfPresent(object, connection, "user");
      copyAttributeIfPresent(object, connection, "password");
      copyAttributeIfPresent(object, connection, "useXaTransactions");
      copyAttributeIfPresent(object, connection, "transactionIsolation");

      report.report("db.jdbcDriverDependency", connection, connection);
    }

    migrateReconnection(connection, object, report);

    final List<Element> configChildren = new ArrayList<>(object.getChildren());
    Collections.reverse(configChildren);
    for (Element element : configChildren) {
      if (element != connection) {
        element.detach();
        if (!"reconnect-forever".equals(element.getName()) && !"reconnect".equals(element.getName())) {
          connection.addContent(0, element);
        }
      }
    }

    object.setName("config");
  }

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }
}
