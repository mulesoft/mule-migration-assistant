/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.file;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
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
 * Migrates the file connector of the file transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class FileConfig extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  private static final String FILE_NAMESPACE_PREFIX = "file";
  private static final String FILE_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/file";
  private static final Namespace FILE_NAMESPACE = Namespace.getNamespace(FILE_NAMESPACE_PREFIX, FILE_NAMESPACE_URI);
  public static final String XPATH_SELECTOR = "/mule:mule/file:connector";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update File connector config.";
  }

  public FileConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(FILE_NAMESPACE));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Namespace fileNs = Namespace.getNamespace(FILE_NAMESPACE_PREFIX, FILE_NAMESPACE_URI);

    handleInputImplicitConnectorRef(object, report);
    handleOutputImplicitConnectorRef(object, report);

    object.setName("config");
    Element connection = new Element("connection", fileNs);
    connection.setAttribute("workingDir", ".");
    object.addContent(connection);

    if (object.getAttribute("streaming") != null && !"true".equals(object.getAttributeValue("streaming"))) {
      report.report(WARN, object, object,
                    "'streaming' is not needed in Mule 4 File Connector, since streams are now repeatable and enabled by default.",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/streaming-about");
    }
    object.removeAttribute("streaming");

    if (object.getAttribute("serialiseObjects") != null && !"false".equals(object.getAttributeValue("serializeObjects"))) {
      report.report(ERROR, object, object,
                    "'serialiseObjects' is not needed in Mule 4 File Connector, you may process the payload with DataWeave directly without the need to convert it to a Java object.",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/transform-component-about");
    }
    object.removeAttribute("serialiseObjects");

    // Not to be confused with Mule 3 workDirectory and Mule 4 workingDir, they may sound similar but have completely different
    // meaning.
    if (object.getAttribute("workDirectory") != null) {
      report.report(WARN, object, object,
                    "'workDirectory' is not needed in Mule 4 File Connector. The source file is locked, so there is no need to move it to a temporary location.",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-file#file_configs");
    }
    object.removeAttribute("workDirectory");

    if (object.getAttribute("workFileNamePattern") != null) {
      report.report(WARN, object, object,
                    "'workFileNamePattern' is not needed in Mule 4 File Connector. The source file is locked, so there is no need to move it to a temporary location.",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-file#file_configs");
    }
    object.removeAttribute("workFileNamePattern");

    String failsDeployment = changeDefault("true", "false", object.getAttributeValue("validateConnections"));
    object.removeAttribute("validateConnections");
    if (failsDeployment != null) {
      Element reconnection = new Element("reconnection", CORE_NAMESPACE);
      reconnection.setAttribute("failsDeployment", failsDeployment);
      connection.addContent(reconnection);
    }

    Element matcher = new Element("matcher", FILE_NAMESPACE_URI);
    matcher.setAttribute("name", object.getAttributeValue("name") + "Matcher");
    boolean matcherUsed = false;

    String fileAge = null;
    if (object.getAttribute("fileAge") != null) {
      fileAge = object.getAttributeValue("fileAge");
      object.removeAttribute("fileAge");
    }

    handleChildElements(object, report, fileNs);
    handleInputSpecificAttributes(object, matcherUsed, fileAge, report);
    handleOutputSpecificAttributes(object, report);
  }

  private void handleInputImplicitConnectorRef(Element object, MigrationReport report) {
    makeImplicitConnectorRefsExplicit(object, report, getApplicationModel()
        .getNodes("/mule:mule/mule:flow/file:inbound-endpoint[not(@connector-ref)]"));
    makeImplicitConnectorRefsExplicit(object, report, getApplicationModel()
        .getNodes("/mule:mule//mule:inbound-endpoint[not(@connector-ref) and starts-with(@address, 'file://')]"));
  }

  private void handleOutputImplicitConnectorRef(Element object, MigrationReport report) {
    makeImplicitConnectorRefsExplicit(object, report,
                                      getApplicationModel().getNodes("/mule:mule//file:outbound-endpoint[not(@connector-ref)]"));
    makeImplicitConnectorRefsExplicit(object, report, getApplicationModel()
        .getNodes("/mule:mule//mule:outbound-endpoint[not(@connector-ref) and starts-with(@address, 'file://')]"));
  }

  private void makeImplicitConnectorRefsExplicit(Element object, MigrationReport report, List<Element> implicitConnectorRefs) {
    List<Element> availableConfigs = getApplicationModel().getNodes("/mule:mule/file:config");
    if (implicitConnectorRefs.size() > 0 && availableConfigs.size() > 1) {
      for (Element implicitConnectorRef : implicitConnectorRefs) {
        // This situation would have caused the app to not start in Mule 3. As it is not a migration issue per se, there's no
        // linked docs
        report.report(ERROR, implicitConnectorRef, implicitConnectorRef,
                      "There are at least 2 connectors matching protocol \"file\","
                          + " so the connector to use must be specified on the endpoint using the 'connector' property/attribute."
                          + " Connectors in your configuration that support \"file\" are: "
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

    Element customFileNameParser = object.getChild("custom-filename-parser", fileNs);
    if (customFileNameParser != null) {
      report.report(ERROR, customFileNameParser, object,
                    "Use a DataWeave expression in <file:write> path attribute to set the filename of the file to write.",
                    "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors-file#file_write");
      object.removeContent(customFileNameParser);
    }

    // Nothing to report here since this is now the default behavior, supporting expressions
    object.removeContent(object.getChild("expression-filename-parser", fileNs));
  }

  private void handleInputSpecificAttributes(Element object, boolean matcherUsed, String fileAge, MigrationReport report) {
    Stream.concat(getApplicationModel()
        .getNodes("/mule:mule//file:inbound-endpoint[@connector-ref='" + object.getAttributeValue("name") + "']")
        .stream(),
                  getApplicationModel()
                      .getNodes("/mule:mule//mule:inbound-endpoint[@connector-ref='" + object.getAttributeValue("name") + "']")
                      .stream())
        .forEach(e -> passConnectorConfigToInboundEnpoint(object, matcherUsed, fileAge, e));

    object.removeAttribute("pollingFrequency");
    object.removeAttribute("readFromDirectory");
    object.removeAttribute("moveToDirectory");
    object.removeAttribute("autoDelete");
    object.removeAttribute("recursive");
    object.removeAttribute("moveToDirectory");
    object.removeAttribute("moveToPattern");
  }

  private void handleOutputSpecificAttributes(Element object, MigrationReport report) {
    Stream.concat(getApplicationModel()
        .getNodes("/mule:mule//file:outbound-endpoint[@connector-ref='" + object.getAttributeValue("name") + "']")
        .stream(),
                  getApplicationModel()
                      .getNodes("/mule:mule//mule:outbound-endpoint[@connector-ref='" + object.getAttributeValue("name") + "']")
                      .stream())
        .forEach(e -> passConnectorConfigToOutboundEndpoint(object, e));

    object.removeAttribute("writeToDirectory");
    object.removeAttribute("outputPattern");
    object.removeAttribute("outputAppend");
  }


  private void passConnectorConfigToInboundEnpoint(Element object, boolean matcherUsed, String fileAge, Element listener) {
    Element schedulingStr = new Element("scheduling-strategy", CORE_NAMESPACE);
    listener.addContent(schedulingStr);
    Element fixedFrequency = new Element("fixed-frequency", CORE_NAMESPACE);
    fixedFrequency.setAttribute("frequency", object.getAttributeValue("pollingFrequency", "1000"));
    schedulingStr.addContent(fixedFrequency);

    if (object.getAttribute("readFromDirectory") != null) {
      listener.setAttribute("directory", object.getAttributeValue("readFromDirectory"));
    }
    if (fileAge != null && !"0".equals(fileAge)) {
      listener.setAttribute("timeBetweenSizeCheck", fileAge);
    }

    String autoDelete = changeDefault("true", "false", object.getAttributeValue("autoDelete"));
    if (autoDelete != null) {
      listener.setAttribute("autoDelete", autoDelete);
    }

    String recursive = changeDefault("false", "true", object.getAttributeValue("recursive"));
    listener.setAttribute("recursive", recursive != null ? recursive : "true");

    if (object.getAttribute("moveToDirectory") != null && listener.getAttribute("moveToDirectory") == null) {
      listener.setAttribute("moveToDirectory", object.getAttributeValue("moveToDirectory"));
    }

    if (object.getAttribute("moveToPattern") != null) {
      String moveToPattern = object.getAttributeValue("moveToPattern");
      listener.setAttribute("renameTo",
                            getExpressionMigrator().migrateExpression(moveToPattern, true, listener));
    }

    if (matcherUsed) {
      listener.setAttribute("matcher", object.getAttributeValue("name") + "Matcher");
    }
  }

  private String changeDefault(String oldDefaultValue, String newDefaultValue, String currentValue) {
    if (currentValue == null) {
      return oldDefaultValue;
    } else if (newDefaultValue.equals(currentValue)) {
      return null;
    } else {
      return currentValue;
    }
  }

  private void passConnectorConfigToOutboundEndpoint(Element object, Element write) {
    if (object.getAttribute("writeToDirectory") != null) {
      write.setAttribute("writeToDirectory", object.getAttributeValue("writeToDirectory"));
    }

    if (object.getAttribute("outputPattern") != null) {
      write.setAttribute("outputPatternConfig", object.getAttributeValue("outputPattern"));
    }

    if (object.getAttribute("outputAppend") != null && !"false".equals(object.getAttributeValue("outputAppend"))) {
      write.setAttribute("mode", "APPEND");
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
