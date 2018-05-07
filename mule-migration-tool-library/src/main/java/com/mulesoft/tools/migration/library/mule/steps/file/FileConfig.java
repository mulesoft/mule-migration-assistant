/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.file;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getElementsFromDocument;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.ExpressionMigrator;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the file connector of the file transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class FileConfig extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  protected static final String FILE_NAMESPACE = "http://www.mulesoft.org/schema/mule/file";

  public static final String XPATH_SELECTOR = "/mule:mule/file:connector";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update File connector config.";
  }

  public FileConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Namespace fileNs = Namespace.getNamespace("file", FILE_NAMESPACE);

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

    Element matcher = new Element("matcher", FILE_NAMESPACE);
    matcher.setAttribute("name", object.getAttributeValue("name") + "Matcher");
    boolean matcherUsed = false;

    if (object.getAttribute("fileAge") != null) {
      int idx = object.getParent().indexOf(object);

      matcher.setAttribute("updatedUntil", object.getAttributeValue("fileAge"));
      matcherUsed = true;

      object.getParent().addContent(idx + 1, matcher);
      object.removeAttribute("fileAge");
    }

    handleChildElements(object, report, fileNs);
    handleInputSpecificAttributes(object, matcherUsed);
    handleOutputSpecificAttributes(object);
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

  private void handleInputSpecificAttributes(Element object, boolean matcherUsed) {
    for (Element listener : getElementsFromDocument(object.getDocument(),
                                                    "/mule:mule/mule:flow/file:inbound-endpoint[@connector-ref='"
                                                        + object.getAttributeValue("name") + "']")) {
      Element schedulingStr = new Element("scheduling-strategy", CORE_NAMESPACE);
      listener.addContent(schedulingStr);
      Element fixedFrequency = new Element("fixed-frequency", CORE_NAMESPACE);
      fixedFrequency.setAttribute("frequency", object.getAttributeValue("pollingFrequency", "1000"));
      schedulingStr.addContent(fixedFrequency);

      if (object.getAttribute("readFromDirectory") != null) {
        listener.setAttribute("directory", object.getAttributeValue("readFromDirectory"));
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
                              getExpressionMigrator().isWrapped(moveToPattern)
                                  ? getExpressionMigrator()
                                      .wrap(getExpressionMigrator().migrateExpression(moveToPattern, true, listener))
                                  : moveToPattern);
      }

      if (matcherUsed) {
        listener.setAttribute("matcher", object.getAttributeValue("name") + "Matcher");
      }
    }
    object.removeAttribute("pollingFrequency");
    object.removeAttribute("readFromDirectory");
    object.removeAttribute("moveToDirectory");
    object.removeAttribute("autoDelete");
    object.removeAttribute("recursive");
    object.removeAttribute("moveToDirectory");
    object.removeAttribute("moveToPattern");
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

  private void handleOutputSpecificAttributes(Element object) {
    for (Element write : getElementsFromDocument(object.getDocument(), "/mule:mule//file:outbound-endpoint[@connector-ref='"
        + object.getAttributeValue("name") + "']")) {
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
    object.removeAttribute("writeToDirectory");
    object.removeAttribute("outputPattern");
    object.removeAttribute("outputAppend");
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
