/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.db;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static java.util.stream.Collectors.toList;
import static org.jdom2.Content.CType.Element;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.ExpressionMigrator;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.xpath.XPathFactory;

import java.util.ArrayList;
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

  private static final String DB_NAMESPACE = "http://www.mulesoft.org/schema/mule/db";

  public static final String XPATH_SELECTOR = "/mule:mule/*[namespace-uri() = '" + DB_NAMESPACE + "']";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update config elements of the DB Connector.";
  }

  public DbConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    final Namespace dbNamespace = Namespace.getNamespace("db", DB_NAMESPACE);

    if ("template-query".equals(object.getName())) {
      List<Element> templateRefs = getApplicationModel().getNodes(XPathFactory.instance()
          .compile("//*[namespace-uri() = '" + DB_NAMESPACE + "' and local-name() = 'template-query-ref' and @name = '"
              + object.getAttributeValue("name") + "']"));

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

    Element dataTypes = object.getChild("data-types", dbNamespace);
    if (dataTypes != null) {
      dataTypes.setName("column-types");
      for (Element dataType : dataTypes.getChildren("data-type", dbNamespace)) {
        dataType.setName("column-type");
        dataType.getAttribute("name").setName("typeName");
      }
    }

    Element connection = null;
    if (object.getAttribute("dataSource-ref") != null) {
      report.report(WARN, object, object,
                    "Mule 3 config has a '" + object.getName()
                        + "' with a dataSource-ref. It was converted to a 'db:data-source-connection'",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-database#example_data_source_db");

      connection = new Element("data-source-connection", dbNamespace);
      copyAttributeIfPresent(object, connection, "dataSource-ref", "dataSourceRef");

      List<Attribute> otherAttributes =
          object.getAttributes().stream().filter(att -> !"name".equals(att.getName())).collect(toList());
      if (!otherAttributes.isEmpty()) {
        report.report(WARN, object, connection,
                      "The attributes " + otherAttributes.toString()
                          + " overlap with properties of the referenced DataSource and were removed",
                      "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-database#example_data_source_db");
      }
    } else if (object.getAttribute("url") != null) {
      connection = new Element("generic-connection", dbNamespace);

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
        } else if ("oracle-config".equals(object.getName())) {
          connection.setAttribute("driverClassName", "oracle.jdbc.driver.OracleDriver");
        }
      }

      report.report(WARN, object, connection,
                    "The config in Mule 3 is specific for an engine, but it contained an 'url' attribute. It will be made generic in order to keep the url.",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-database#example_generic_db");

      Element connectionProps = object.getChild("connection-properties", dbNamespace);
      if (connectionProps != null) {
        // Have to use isPresent() because connection cannot be final
        Optional<Element> userProp = connectionProps.getChildren("property", dbNamespace)
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
      connection = new Element("derby-connection", dbNamespace);
      copyAttributeIfPresent(object, connection, "user");
      copyAttributeIfPresent(object, connection, "password");
      copyAttributeIfPresent(object, connection, "useXaTransactions");
      copyAttributeIfPresent(object, connection, "transactionIsolation");
    } else if ("mysql-config".equals(object.getName())) {
      connection = new Element("my-sql-connection", dbNamespace);
      copyAttributeIfPresent(object, connection, "database");
      copyAttributeIfPresent(object, connection, "host");
      copyAttributeIfPresent(object, connection, "port");
      copyAttributeIfPresent(object, connection, "user");
      copyAttributeIfPresent(object, connection, "password");
      copyAttributeIfPresent(object, connection, "useXaTransactions");
      copyAttributeIfPresent(object, connection, "transactionIsolation");
    } else if ("oracle-config".equals(object.getName())) {
      connection = new Element("oracle-connection", dbNamespace);
      copyAttributeIfPresent(object, connection, "host");
      copyAttributeIfPresent(object, connection, "port");
      copyAttributeIfPresent(object, connection, "instance");
      copyAttributeIfPresent(object, connection, "user");
      copyAttributeIfPresent(object, connection, "password");
      copyAttributeIfPresent(object, connection, "useXaTransactions");
      copyAttributeIfPresent(object, connection, "transactionIsolation");
    }

    report.report(ERROR, connection, connection,
                  "Add a suitable jdbc driver dependency for this connection",
                  "https://docs.mulesoft.com/connectors/db-configure-connection#setting-the-jdbc-driver");

    for (Element element : new ArrayList<>(object.getChildren())) {
      element.detach();
      connection.addContent(element);
    }

    Element reconnect = connection.getChild("reconnect", CORE_NAMESPACE);
    if (reconnect != null) {
      // TODO migrate reconnections
      report.report(ERROR, reconnect, connection,
                    "Reconnection notifiers cannot be configured on the 'reconnect' element",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-database#reconnection_strategies");
      connection.removeContent(reconnect);
    }

    connection.addContent(new Element("reconnection", CORE_NAMESPACE).setAttribute("failsDeployment", "true"));

    object.setName("config");
    object.addContent(connection);

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
