/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.mulesoft.tools.migration.library.mule.steps.file;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.handleConnectorChildElements;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.handleServiceOverrides;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.changeDefault;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateReconnection;
import static java.util.stream.Collectors.joining;
import static org.jdom2.Namespace.getNamespace;

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
  public static final String FILE_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/file";
  public static final Namespace FILE_NAMESPACE = getNamespace(FILE_NAMESPACE_PREFIX, FILE_NAMESPACE_URI);
  public static final String XPATH_SELECTOR =
      "/*/*[namespace-uri()='" + FILE_NAMESPACE_URI + "' and local-name()='connector']";

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
    handleServiceOverrides(object, report);

    handleInputImplicitConnectorRef(object, report);
    handleOutputImplicitConnectorRef(object, report);

    object.setName("config");
    Element connection = new Element("connection", FILE_NAMESPACE);
    connection.setAttribute("workingDir", ".");
    object.addContent(connection);

    if (object.getAttribute("streaming") != null && !"true".equals(object.getAttributeValue("streaming"))) {
      report.report("file.streaming", object, object);
    }
    object.removeAttribute("streaming");

    if (object.getAttribute("serialiseObjects") != null && !"false".equals(object.getAttributeValue("serializeObjects"))) {
      report.report("file.serialiseObjects", object, object);
    }
    object.removeAttribute("serialiseObjects");

    // Not to be confused with Mule 3 workDirectory and Mule 4 workingDir, they may sound similar but have completely different
    // meaning.
    if (object.getAttribute("workDirectory") != null) {
      report.report("file.workDirectory", object, object);
    }
    object.removeAttribute("workDirectory");

    if (object.getAttribute("workFileNamePattern") != null) {
      report.report("file.workFileNamePattern", object, object);
    }
    object.removeAttribute("workFileNamePattern");

    migrateReconnection(connection, object, report);

    Element matcher = new Element("matcher", FILE_NAMESPACE_URI);
    matcher.setAttribute("name", object.getAttributeValue("name") + "Matcher");
    boolean matcherUsed = false;

    String fileAge = null;
    if (object.getAttribute("fileAge") != null) {
      fileAge = object.getAttributeValue("fileAge");
      object.removeAttribute("fileAge");
    }

    handleChildElements(object, connection, report);
    handleInputSpecificAttributes(object, matcherUsed, fileAge, report);
    handleOutputSpecificAttributes(object, report);
  }

  private void handleInputImplicitConnectorRef(Element object, MigrationReport report) {
    makeImplicitConnectorRefsExplicit(object, report, getApplicationModel()
        .getNodes("/*/mule:flow/*[namespace-uri()='" + FILE_NAMESPACE_URI
            + "' and local-name()='inbound-endpoint' and not(@connector-ref)]"));
    makeImplicitConnectorRefsExplicit(object, report, getApplicationModel()
        .getNodes("//mule:inbound-endpoint[not(@connector-ref) and starts-with(@address, 'file://')]"));
  }

  private void handleOutputImplicitConnectorRef(Element object, MigrationReport report) {
    makeImplicitConnectorRefsExplicit(object, report,
                                      getApplicationModel().getNodes("//*[namespace-uri()='" + FILE_NAMESPACE_URI
                                          + "' and local-name()='outbound-endpoint' and not(@connector-ref)]"));
    makeImplicitConnectorRefsExplicit(object, report, getApplicationModel()
        .getNodes("//mule:outbound-endpoint[not(@connector-ref) and starts-with(@address, 'file://')]"));
  }

  private void makeImplicitConnectorRefsExplicit(Element object, MigrationReport report, List<Element> implicitConnectorRefs) {
    List<Element> availableConfigs =
        getApplicationModel().getNodes("/*/*[namespace-uri()='" + FILE_NAMESPACE_URI + "' and local-name()='config']");
    if (implicitConnectorRefs.size() > 0 && availableConfigs.size() > 1) {
      for (Element implicitConnectorRef : implicitConnectorRefs) {
        // This situation would have caused the app to not start in Mule 3. As it is not a migration issue per se, there's no
        // linked docs
        report.report("transports.manyConnectors", implicitConnectorRef, implicitConnectorRef,
                      "file", availableConfigs.stream().map(e -> e.getAttributeValue("name")).collect(joining(", ")));
      }
    } else {
      for (Element implicitConnectorRef : implicitConnectorRefs) {
        implicitConnectorRef.setAttribute("connector-ref", object.getAttributeValue("name"));
      }
    }
  }

  public static void handleChildElements(Element object, Element connection, MigrationReport report) {
    handleConnectorChildElements(object, object, connection, report);

    Element customFileNameParser = object.getChild("custom-filename-parser", FILE_NAMESPACE);
    if (customFileNameParser != null) {
      report.report("file.filePath", customFileNameParser, object);
      object.removeContent(customFileNameParser);
    }

    // Nothing to report here since this is now the default behavior, supporting expressions
    object.removeContent(object.getChild("expression-filename-parser", FILE_NAMESPACE));
  }

  private void handleInputSpecificAttributes(Element object, boolean matcherUsed, String fileAge, MigrationReport report) {
    Stream.concat(getApplicationModel()
        .getNodes("//*[namespace-uri()='" + FILE_NAMESPACE_URI + "' and local-name()='inbound-endpoint' and @connector-ref='"
            + object.getAttributeValue("name") + "']")
        .stream(),
                  getApplicationModel()
                      .getNodes("//mule:inbound-endpoint[@connector-ref='" + object.getAttributeValue("name") + "']")
                      .stream())
        .forEach(e -> passConnectorConfigToInboundEnpoint(object, matcherUsed, fileAge, e));

    object.removeAttribute("pollingFrequency");
    object.removeAttribute("readFromDirectory");
    object.removeAttribute("autoDelete");
    object.removeAttribute("recursive");
    object.removeAttribute("moveToDirectory");
    object.removeAttribute("moveToPattern");
  }

  private void handleOutputSpecificAttributes(Element object, MigrationReport report) {
    Stream.concat(getApplicationModel()
        .getNodes("//*[namespace-uri()='" + FILE_NAMESPACE_URI + "' and local-name()='outbound-endpoint' and @connector-ref='"
            + object.getAttributeValue("name") + "']")
        .stream(),
                  getApplicationModel()
                      .getNodes("//mule:outbound-endpoint[@connector-ref='" + object.getAttributeValue("name") + "']")
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
