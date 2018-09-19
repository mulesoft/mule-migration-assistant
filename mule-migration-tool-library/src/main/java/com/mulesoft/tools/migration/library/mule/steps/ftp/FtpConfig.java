/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.ftp;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.library.mule.steps.file.FileConfig.handleChildElements;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.changeDefault;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static java.util.stream.Collectors.joining;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.List;
import java.util.stream.Stream;

/**
 * Migrates the ftp connector of the fto transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class FtpConfig extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  private static final String FTP_NAMESPACE_PREFIX = "ftp";
  private static final String FTP_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/ftp";
  public static final Namespace FTP_NAMESPACE = Namespace.getNamespace(FTP_NAMESPACE_PREFIX, FTP_NAMESPACE_URI);
  public static final String XPATH_SELECTOR =
      "/*/*[namespace-uri() = '" + FTP_NAMESPACE_URI + "' and local-name() = 'connector']";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update FTP connector config.";
  }

  public FtpConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(FTP_NAMESPACE));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Namespace ftpNs = Namespace.getNamespace(FTP_NAMESPACE_PREFIX, FTP_NAMESPACE_URI);

    handleInputImplicitConnectorRef(object, report);
    handleOutputImplicitConnectorRef(object, report);

    object.setName("config");
    object.setNamespace(FTP_NAMESPACE);

    Element connection = new Element("connection", ftpNs);
    // connection.setAttribute("workingDir", ".");
    object.addContent(connection);

    if (object.getAttribute("streaming") != null && !"true".equals(object.getAttributeValue("streaming"))) {
      report.report(WARN, object, object,
                    "'streaming' is not needed in Mule 4 FTP Connector, since streams are now repeatable and enabled by default.",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/streaming-about");
    }
    object.removeAttribute("streaming");

    if (object.getAttribute("connectionFactoryClass") != null
        && !"true".equals(object.getAttributeValue("connectionFactoryClass"))) {
      report.report(ERROR, object, object, "'connectionFactoryClass' cannot be changed in Mule 4 FTP Connector.",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-ftp-sftp#migrating-an-ftp-connection");
    }
    object.removeAttribute("connectionFactoryClass");

    String failsDeployment = changeDefault("true", "false", object.getAttributeValue("validateConnections"));
    object.removeAttribute("validateConnections");
    if (failsDeployment != null) {
      Element reconnection = new Element("reconnection", CORE_NAMESPACE);
      reconnection.setAttribute("failsDeployment", failsDeployment);
      connection.addContent(reconnection);
    }

    copyAttributeIfPresent(object, connection, "passive");
    if (object.getAttribute("binary") != null) {
      connection.setAttribute("transferMode", "true".equals(object.getAttributeValue("binary")) ? "BINARY" : "ASCII");
      object.removeAttribute("binary");
    }

    if (object.getAttribute("connectionTimeout") != null) {
      copyAttributeIfPresent(object, connection, "connectionTimeout", "connectionTimeout");
      connection.setAttribute("connectionTimeoutUnit", "MILLISECONDS");
    }

    handleChildElements(object, connection, report);
    handleInputSpecificAttributes(object, report);
    handleOutputSpecificAttributes(object, report);
  }

  private void handleInputImplicitConnectorRef(Element object, MigrationReport report) {
    makeImplicitConnectorRefsExplicit(object, report, getApplicationModel()
        .getNodes("/*/mule:flow/ftp:inbound-endpoint[not(@connector-ref)]"));
    makeImplicitConnectorRefsExplicit(object, report, getApplicationModel()
        .getNodes("//mule:inbound-endpoint[not(@connector-ref) and starts-with(@address, 'ftp://')]"));
  }

  private void handleOutputImplicitConnectorRef(Element object, MigrationReport report) {
    makeImplicitConnectorRefsExplicit(object, report,
                                      getApplicationModel().getNodes("//ftp:outbound-endpoint[not(@connector-ref)]"));
    makeImplicitConnectorRefsExplicit(object, report, getApplicationModel()
        .getNodes("//mule:outbound-endpoint[not(@connector-ref) and starts-with(@address, 'ftp://')]"));
  }

  private void makeImplicitConnectorRefsExplicit(Element object, MigrationReport report, List<Element> implicitConnectorRefs) {
    List<Element> availableConfigs = getApplicationModel().getNodes("/*/ftp:config");
    if (implicitConnectorRefs.size() > 0 && availableConfigs.size() > 1) {
      for (Element implicitConnectorRef : implicitConnectorRefs) {
        // This situation would have caused the app to not start in Mule 3. As it is not a migration issue per se, there's no
        // linked docs
        report.report(ERROR, implicitConnectorRef, implicitConnectorRef,
                      "There are at least 2 connectors matching protocol \"ftp\","
                          + " so the connector to use must be specified on the endpoint using the 'connector' property/attribute."
                          + " Connectors in your configuration that support \"ftp\" are: "
                          + availableConfigs.stream().map(e -> e.getAttributeValue("name")).collect(joining(", ")));
      }
    } else {
      for (Element implicitConnectorRef : implicitConnectorRefs) {
        implicitConnectorRef.setAttribute("connector-ref", object.getAttributeValue("name"));
      }
    }
  }

  private void handleInputSpecificAttributes(Element object, MigrationReport report) {
    Stream.concat(getApplicationModel()
        .getNodes("//ftp:inbound-endpoint[@connector-ref='" + object.getAttributeValue("name") + "']")
        .stream(),
                  getApplicationModel()
                      .getNodes("//mule:inbound-endpoint[@connector-ref='" + object.getAttributeValue("name") + "']")
                      .stream())
        .forEach(e -> passConnectorConfigToInboundEnpoint(object, e));

    object.removeAttribute("pollingFrequency");
  }

  private void handleOutputSpecificAttributes(Element object, MigrationReport report) {
    Stream.concat(getApplicationModel()
        .getNodes("//ftp:outbound-endpoint[@connector-ref='" + object.getAttributeValue("name") + "']")
        .stream(),
                  getApplicationModel()
                      .getNodes("//mule:outbound-endpoint[@connector-ref='" + object.getAttributeValue("name") + "']")
                      .stream())
        .forEach(e -> passConnectorConfigToOutboundEndpoint(object, e));

    object.removeAttribute("outputPattern");
  }


  private void passConnectorConfigToInboundEnpoint(Element object, Element listener) {
    Element schedulingStr = new Element("scheduling-strategy", CORE_NAMESPACE);
    listener.addContent(schedulingStr);
    Element fixedFrequency = new Element("fixed-frequency", CORE_NAMESPACE);
    fixedFrequency.setAttribute("frequency", object.getAttributeValue("pollingFrequency", "1000"));
    schedulingStr.addContent(fixedFrequency);
  }

  private void passConnectorConfigToOutboundEndpoint(Element object, Element write) {
    if (object.getAttribute("outputPattern") != null) {
      write.setAttribute("outputPatternConfig", object.getAttributeValue("outputPattern"));
    }
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
