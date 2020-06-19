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
package com.mulesoft.tools.migration.library.mule.steps.email;

import static com.mulesoft.tools.migration.step.util.TransportsUtils.handleServiceOverrides;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.migrateSchedulingStrategy;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.processAddress;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addMigrationAttributeToElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.migrateReconnection;

import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Attribute;
import org.jdom2.Element;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * Migrates the Pop3 inbound endpoint of the Email Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class Pop3InboundEndpoint extends AbstractEmailSourceMigrator implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR =
      "/*/mule:flow/*[namespace-uri()='" + POP3_NAMESPACE_URI + "' and local-name()='inbound-endpoint'][1]";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update Pop3 transport inbound endpoint.";
  }

  public Pop3InboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.setName("listener-pop3");
    object.setNamespace(EMAIL_NAMESPACE);

    addMigrationAttributeToElement(object, new Attribute("isMessageSource", "true"));
    addAttributesToInboundProperties(object, report);

    Optional<Element> pop3Connector = resolveConnector(object, getApplicationModel());

    getApplicationModel().addNameSpace(EMAIL_NAMESPACE.getPrefix(), EMAIL_NAMESPACE.getURI(), EMAIL_SCHEMA_LOC);

    migrateSchedulingStrategy(object, OptionalInt.empty());
    Element fixedFrequency = object.getChild("scheduling-strategy", CORE_NAMESPACE).getChild("fixed-frequency", CORE_NAMESPACE);

    pop3Connector.ifPresent(c -> {
      handleServiceOverrides(c, report);
      migrateReconnection(c, object, report);

      if (c.getAttribute("moveToFolder") != null) {
        // TODO https://www.mulesoft.org/jira/browse/MULE-15721
        report.report("email.moveToFolder", object, c);
      }

      if (c.getAttribute("mailboxFolder") != null) {
        object.setAttribute("folder", c.getAttributeValue("mailboxFolder"));
      }
      if (c.getAttribute("backupEnabled") != null || c.getAttribute("backupFolder") != null) {
        report.report("email.pop3Backup", object, object);
      }

      if (c.getAttribute("deleteReadMessages") != null) {
        object.setAttribute("deleteAfterRetrieve", c.getAttributeValue("deleteReadMessages"));
      }
      if (c.getAttribute("defaultProcessMessageAction") != null) {
        object.removeAttribute("defaultProcessMessageAction");
        report.report("email.pop3DefaultProcessMessageAction", object, object);
      }

      if (c.getAttribute("checkFrequency") != null) {
        fixedFrequency.setAttribute("frequency", c.getAttributeValue("checkFrequency"));
      }
    });

    Element m4Config = migratePop3Config(object, report, pop3Connector);
    Element connection = getConnection(m4Config);

    if (pop3Connector.isPresent() && "gmail-connector".equals(pop3Connector.get().getName())) {
      connection.setName("pop3s-connection");
      connection.addContent(new Element("context", TLS_NAMESPACE)
          .addContent(new Element("trust-store", TLS_NAMESPACE).setAttribute("insecure", "true")));

      connection.setAttribute("host", "pop.gmail.com");
      connection.setAttribute("port", "995");
      object.removeAttribute("host");
      object.removeAttribute("port");

      getApplicationModel().addNameSpace(TLS_NAMESPACE.getPrefix(), TLS_NAMESPACE.getURI(),
                                         "http://www.mulesoft.org/schema/mule/tls/current/mule-tls.xsd");

      report.report("email.gmail", pop3Connector.get(), connection);
    } else {
      processAddress(object, report).ifPresent(address -> {
        connection.setAttribute("host", address.getHost());
        connection.setAttribute("port", address.getPort());

        if (address.getCredentials() != null) {
          String[] credsSplit = address.getCredentials().split(":");

          connection.setAttribute("user", credsSplit[0]);
          connection.setAttribute("password", credsSplit[1]);
        }
      });
      copyAttributeIfPresent(object, connection, "host");
      copyAttributeIfPresent(object, connection, "port");
    }

    copyAttributeIfPresent(object, connection, "user");
    copyAttributeIfPresent(object, connection, "password");

    if (object.getAttribute("connector-ref") != null) {
      object.getAttribute("connector-ref").setName("config-ref");
    } else {
      object.removeAttribute("name");
      object.setAttribute("config-ref", m4Config.getAttributeValue("name"));
    }

    if (object.getAttribute("responseTimeout") != null) {
      connection.setAttribute("readTimeout", object.getAttributeValue("responseTimeout"));
      connection.setAttribute("writeTimeout", object.getAttributeValue("responseTimeout"));
      connection.setAttribute("timeoutUnit", "MILLISECONDS");
      object.removeAttribute("responseTimeout");
    }
  }

  @Override
  protected Element getConnector(String connectorName) {
    return getApplicationModel().getNode("/*/*[namespace-uri()='" + POP3_NAMESPACE_URI
        + "' and (local-name()='connector' or local-name()='gmail-connector') and @name = '" + connectorName + "']");
  }

  protected Element getConnection(Element m4Config) {
    return m4Config.getChild("pop3-connection", EMAIL_NAMESPACE);
  }

  @Override
  protected Optional<Element> getDefaultConnector() {
    return getApplicationModel()
        .getNodeOptional("/*/*[namespace-uri()='" + POP3_NAMESPACE_URI
            + "' and (local-name()='connector' or local-name()='gmail-connector')]");
  }

  public Element migratePop3Config(Element object, MigrationReport report, Optional<Element> connector) {
    String configName = connector.map(conn -> conn.getAttributeValue("name")).orElse((object.getAttribute("name") != null
        ? object.getAttributeValue("name")
        : (object.getAttribute("ref") != null
            ? object.getAttributeValue("ref")
            : "")).replaceAll("\\\\", "_")
        + "Pop3Config");

    Optional<Element> config = getApplicationModel()
        .getNodeOptional("*/*[namespace-uri() = '" + EMAIL_NAMESPACE.getURI() + "' and local-name() = 'pop3-config' and @name='"
            + configName + "']");
    return config.orElseGet(() -> {
      final Element imapCfg = new Element("pop3-config", EMAIL_NAMESPACE);
      imapCfg.setAttribute("name", configName);

      Element connection = createConnection();
      imapCfg.addContent(connection);

      addTopLevelElement(imapCfg, connector.map(c -> c.getDocument()).orElse(object.getDocument()));

      return imapCfg;
    });
  }

  protected Element createConnection() {
    return new Element("pop3-connection", EMAIL_NAMESPACE);
  }

  @Override
  protected String getInboundAttributesClass() {
    return "org.mule.extension.email.api.attributes.POP3EmailAttributes";
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
