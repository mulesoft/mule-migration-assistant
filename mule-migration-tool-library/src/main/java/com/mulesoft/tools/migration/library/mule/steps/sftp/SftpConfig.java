/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.sftp;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.library.mule.steps.file.FileConfig.handleChildElements;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.handleReconnection;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.handleServiceOverrides;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
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
 * Migrates the sftp connector of the sfto transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SftpConfig extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  private static final String SFTP_NAMESPACE_PREFIX = "sftp";
  private static final String SFTP_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/sftp";
  public static final Namespace SFTP_NAMESPACE = Namespace.getNamespace(SFTP_NAMESPACE_PREFIX, SFTP_NAMESPACE_URI);
  public static final String XPATH_SELECTOR =
      "/*/*[namespace-uri() = '" + SFTP_NAMESPACE_URI + "' and local-name() = 'connector']";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update SFTP connector config.";
  }

  public SftpConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(SFTP_NAMESPACE));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    handleServiceOverrides(object, report);

    handleInputImplicitConnectorRef(object, report);
    handleOutputImplicitConnectorRef(object, report);

    object.setName("config");
    object.setNamespace(SFTP_NAMESPACE);

    Element connection = new Element("connection", SFTP_NAMESPACE);
    object.addContent(connection);

    if (object.getAttribute("maxConnectionPoolSize") != null && !"0".equals(object.getAttributeValue("maxConnectionPoolSize"))) {
      int maxConnectionPoolSize = Integer.valueOf(object.getAttributeValue("maxConnectionPoolSize"));

      connection.addContent(new Element("pooling-profile", CORE_NAMESPACE)
          .setAttribute("exhaustedAction", "WHEN_EXHAUSTED_WAIT")
          .setAttribute("maxActive", "" + maxConnectionPoolSize)
          // 8 is the default value in the Mule 3 transport
          .setAttribute("maxIdle", "" + Math.min(8, maxConnectionPoolSize))
          .setAttribute("maxWait", "-1"));

    }
    object.removeAttribute("maxConnectionPoolSize");

    handleReconnection(object, connection);

    Element proxyConfig = object.getChild("proxy-config", SFTP_NAMESPACE);
    if (proxyConfig != null) {
      proxyConfig.setName("sftp-proxy-config");
      connection.addContent(proxyConfig.detach());
    }

    if (object.getAttribute("connectionTimeout") != null) {
      copyAttributeIfPresent(object, connection, "connectionTimeout", "connectionTimeout");
      connection.setAttribute("connectionTimeoutUnit", "MILLISECONDS");
    }

    copyAttributeIfPresent(object, connection, "identityFile");
    copyAttributeIfPresent(object, connection, "passphrase");
    copyAttributeIfPresent(object, connection, "preferredAuthenticationMethods");
    copyAttributeIfPresent(object, connection, "knownHostsFile");

    handleChildElements(object, connection, report);
    handleInputSpecificAttributes(object, report);
    handleOutputSpecificAttributes(object, report);

    object.removeAttribute("autoDelete");
  }

  private void handleInputImplicitConnectorRef(Element object, MigrationReport report) {
    makeImplicitConnectorRefsExplicit(object, report, getApplicationModel()
        .getNodes("/*/mule:flow/sftp:inbound-endpoint[not(@connector-ref)]"));
    makeImplicitConnectorRefsExplicit(object, report, getApplicationModel()
        .getNodes("//mule:inbound-endpoint[not(@connector-ref) and starts-with(@address, 'sftp://')]"));
  }

  private void handleOutputImplicitConnectorRef(Element object, MigrationReport report) {
    makeImplicitConnectorRefsExplicit(object, report,
                                      getApplicationModel().getNodes("//*[namespace-uri()='" + SFTP_NAMESPACE_URI
                                          + "' and local-name()='outbound-endpoint' and not(@connector-ref)]"));
    makeImplicitConnectorRefsExplicit(object, report, getApplicationModel()
        .getNodes("//mule:outbound-endpoint[not(@connector-ref) and starts-with(@address, 'sftp://')]"));
  }

  private void makeImplicitConnectorRefsExplicit(Element object, MigrationReport report, List<Element> implicitConnectorRefs) {
    List<Element> availableConfigs =
        getApplicationModel().getNodes("/*/*[namespace-uri()='" + SFTP_NAMESPACE_URI + "' and local-name()='config']");
    if (implicitConnectorRefs.size() > 0 && availableConfigs.size() > 1) {
      for (Element implicitConnectorRef : implicitConnectorRefs) {
        // This situation would have caused the app to not start in Mule 3. As it is not a migration issue per se, there's no
        // linked docs
        report.report("transports.manyConnectors", implicitConnectorRef, implicitConnectorRef,
                      "sftp", availableConfigs.stream().map(e -> e.getAttributeValue("name")).collect(joining(", ")));
      }
    } else {
      for (Element implicitConnectorRef : implicitConnectorRefs) {
        implicitConnectorRef.setAttribute("connector-ref", object.getAttributeValue("name"));
      }
    }
  }

  private void handleInputSpecificAttributes(Element object, MigrationReport report) {
    Stream.concat(getApplicationModel()
        .getNodes("//*[namespace-uri()='" + SFTP_NAMESPACE_URI + "' and local-name()='inbound-endpoint' and @connector-ref='"
            + object.getAttributeValue("name") + "']")
        .stream(),
                  getApplicationModel()
                      .getNodes("//mule:inbound-endpoint[@connector-ref='" + object.getAttributeValue("name") + "']")
                      .stream())
        .forEach(e -> passConnectorConfigToInboundEnpoint(object, e));

    object.removeAttribute("pollingFrequency");
    object.removeAttribute("fileAge");
    object.removeAttribute("sizeCheckWaitTime");
    object.removeAttribute("tempDirInbound");
    object.removeAttribute("useTempFileTimestampSuffix");
  }

  private void handleOutputSpecificAttributes(Element object, MigrationReport report) {
    Stream.concat(getApplicationModel()
        .getNodes("//*[namespace-uri()='" + SFTP_NAMESPACE_URI + "' and local-name()='outbound-endpoint' and @connector-ref='"
            + object.getAttributeValue("name") + "']")
        .stream(),
                  getApplicationModel()
                      .getNodes("//mule:outbound-endpoint[@connector-ref='" + object.getAttributeValue("name") + "']")
                      .stream())
        .forEach(e -> passConnectorConfigToOutboundEndpoint(object, e));

    object.removeAttribute("outputPattern");
    object.removeAttribute("tempDirOutbound");
    object.removeAttribute("duplicateHandling");
  }

  private void passConnectorConfigToInboundEnpoint(Element object, Element listener) {
    Element schedulingStr = new Element("scheduling-strategy", CORE_NAMESPACE);
    listener.addContent(schedulingStr);
    Element fixedFrequency = new Element("fixed-frequency", CORE_NAMESPACE);
    fixedFrequency.setAttribute("frequency", object.getAttributeValue("pollingFrequency", "1000"));
    schedulingStr.addContent(fixedFrequency);

    if (listener.getAttribute("autoDelete") == null && object.getAttribute("autoDelete") != null) {
      listener.setAttribute("autoDelete", object.getAttributeValue("autoDelete"));
    }

    if (listener.getAttribute("fileAge") == null && object.getAttribute("fileAge") != null) {
      listener.setAttribute("fileAge", object.getAttributeValue("fileAge"));
    }
    if (listener.getAttribute("sizeCheckWaitTime") == null && object.getAttribute("sizeCheckWaitTime") != null) {
      listener.setAttribute("sizeCheckWaitTime", object.getAttributeValue("sizeCheckWaitTime"));
    }

    if (listener.getAttribute("tempDirInbound") == null && object.getAttribute("tempDir") != null) {
      listener.setAttribute("tempDir", object.getAttributeValue("tempDirInbound"));
    }
    if (listener.getAttribute("useTempFileTimestampSuffix") == null
        && object.getAttribute("useTempFileTimestampSuffix") != null) {
      listener.setAttribute("useTempFileTimestampSuffix", object.getAttributeValue("useTempFileTimestampSuffix"));
    }
  }

  private void passConnectorConfigToOutboundEndpoint(Element object, Element write) {
    if (object.getAttribute("outputPattern") != null) {
      write.setAttribute("outputPatternConfig", object.getAttributeValue("outputPattern"));
    }

    if (write.getAttribute("tempDirOutbound") == null && object.getAttribute("tempDir") != null) {
      write.setAttribute("tempDir", object.getAttributeValue("tempDirOutbound"));
    }

    if (write.getAttribute("duplicateHandling") == null && object.getAttribute("duplicateHandling") != null) {
      write.setAttribute("duplicateHandling", object.getAttributeValue("duplicateHandling"));
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
