/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.ftp;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.changeDefault;
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
  public static final String XPATH_SELECTOR = "/*/ftp:connector";

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
    Element connection = new Element("connection", ftpNs);
    // connection.setAttribute("workingDir", ".");
    object.addContent(connection);

    if (object.getAttribute("streaming") != null && !"true".equals(object.getAttributeValue("streaming"))) {
      report.report(WARN, object, object,
                    "'streaming' is not needed in Mule 4 File Connector, since streams are now repeatable and enabled by default.",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/streaming-about");
    }
    object.removeAttribute("streaming");

    String failsDeployment = changeDefault("true", "false", object.getAttributeValue("validateConnections"));
    object.removeAttribute("validateConnections");
    if (failsDeployment != null) {
      Element reconnection = new Element("reconnection", CORE_NAMESPACE);
      reconnection.setAttribute("failsDeployment", failsDeployment);
      connection.addContent(reconnection);
    }

    // Element matcher = new Element("matcher", FTP_NAMESPACE_URI);
    // matcher.setAttribute("name", object.getAttributeValue("name") + "Matcher");
    // boolean matcherUsed = false;
    //
    // String fileAge = null;
    // if (object.getAttribute("fileAge") != null) {
    // fileAge = object.getAttributeValue("fileAge");
    // object.removeAttribute("fileAge");
    // }

    handleChildElements(object, report, ftpNs);
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

  private void handleChildElements(Element object, MigrationReport report, Namespace fileNs) {
    Element receiverThreadingProfile = object.getChild("receiver-threading-profile", CORE_NAMESPACE);
    if (receiverThreadingProfile != null) {
      report.report(WARN, receiverThreadingProfile, object,
                    "Threading profiles do not exist in Mule 4. This may be replaced by a 'maxConcurrency' value in the flow.",
                    "https://docs.mulesoft.com/mule-user-guide/v/4.1/intro-engine");
      object.removeContent(receiverThreadingProfile);
    }

    Element dispatcherThreadingProfile = object.getChild("dispatcher-threading-profile", CORE_NAMESPACE);
    if (dispatcherThreadingProfile != null) {
      report.report(WARN, dispatcherThreadingProfile, object,
                    "Threading profiles do not exist in Mule 4. This may be replaced by a 'maxConcurrency' value in the flow.",
                    "https://docs.mulesoft.com/mule-user-guide/v/4.1/intro-engine");
      object.removeContent(dispatcherThreadingProfile);
    }

    // Element customFileNameParser = object.getChild("custom-filename-parser", fileNs);
    // if (customFileNameParser != null) {
    // report.report(ERROR, customFileNameParser, object,
    // "Use a DataWeave expression in <file:write> path attribute to set the filename of the file to write.",
    // "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-file#file_write");
    // object.removeContent(customFileNameParser);
    // }
    //
    // // Nothing to report here since this is now the default behavior, supporting expressions
    // object.removeContent(object.getChild("expression-filename-parser", fileNs));
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
    // object.removeAttribute("readFromDirectory");
    // object.removeAttribute("moveToDirectory");
    // object.removeAttribute("autoDelete");
    // object.removeAttribute("recursive");
    // object.removeAttribute("moveToDirectory");
    // object.removeAttribute("moveToPattern");
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

    // if (object.getAttribute("readFromDirectory") != null) {
    // listener.setAttribute("directory", object.getAttributeValue("readFromDirectory"));
    // }
    // if (fileAge != null && !"0".equals(fileAge)) {
    // listener.setAttribute("timeBetweenSizeCheck", fileAge);
    // }
    //
    // String autoDelete = changeDefault("true", "false", object.getAttributeValue("autoDelete"));
    // if (autoDelete != null) {
    // listener.setAttribute("autoDelete", autoDelete);
    // }
    //
    // String recursive = changeDefault("false", "true", object.getAttributeValue("recursive"));
    // listener.setAttribute("recursive", recursive != null ? recursive : "true");
    //
    // if (object.getAttribute("moveToDirectory") != null && listener.getAttribute("moveToDirectory") == null) {
    // listener.setAttribute("moveToDirectory", object.getAttributeValue("moveToDirectory"));
    // }
    //
    // if (object.getAttribute("moveToPattern") != null) {
    // String moveToPattern = object.getAttributeValue("moveToPattern");
    // listener.setAttribute("renameTo",
    // getExpressionMigrator().migrateExpression(moveToPattern, true, listener));
    // }
    //
    // if (matcherUsed) {
    // listener.setAttribute("matcher", object.getAttributeValue("name") + "Matcher");
    // }
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
