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

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.Optional;

/**
 * Migrates the imaps inbound endpoint of the Email Transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ImapsInboundEndpoint extends ImapInboundEndpoint {

  public static final String XPATH_SELECTOR =
      "/*/mule:flow/*[namespace-uri()='" + IMAPS_NAMESPACE_URI + "' and local-name()='inbound-endpoint'][1]";

  @Override
  public String getDescription() {
    return "Update IMaps transport inbound endpoint.";
  }

  public ImapsInboundEndpoint() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Optional<Element> imapsConnector;
    if (object.getAttribute("connector-ref") != null) {
      imapsConnector = Optional.of(getConnector(object.getAttributeValue("connector-ref")));
    } else {
      imapsConnector = getDefaultConnector();
    }

    super.execute(object, report);

    Element imapsConnection = getApplicationModel()
        .getNode("/*/*[namespace-uri() = '" + EMAIL_NAMESPACE.getURI() + "' and local-name() = 'imap-config' and @name = '"
            + object.getAttributeValue("config-ref")
            + "']/*[namespace-uri() = '" + EMAIL_NAMESPACE.getURI() + "' and local-name() = 'imaps-connection']");

    if (imapsConnector.isPresent() && imapsConnection.getChild("context", TLS_NAMESPACE) == null) {

      Element tlsContext = new Element("context", TLS_NAMESPACE);
      boolean tlsConfigured = false;

      Namespace imapsNamespace = getNamespace("imaps", "http://www.mulesoft.org/schema/mule/imaps");
      Element tlsKeyStore = imapsConnector.get().getChild("tls-client", imapsNamespace);
      if (tlsKeyStore != null) {
        Element keyStore = new Element("key-store", TLS_NAMESPACE);
        copyAttributeIfPresent(tlsKeyStore, keyStore, "path");
        copyAttributeIfPresent(tlsKeyStore, keyStore, "storePassword", "password");
        copyAttributeIfPresent(tlsKeyStore, keyStore, "keyPassword");
        if (tlsKeyStore.getAttribute("class") != null) {
          report.report("email.imapKeyStoreClass", tlsKeyStore, tlsKeyStore);
        }
        copyAttributeIfPresent(tlsKeyStore, keyStore, "type", "type");
        copyAttributeIfPresent(tlsKeyStore, keyStore, "keyAlias", "alias");
        copyAttributeIfPresent(tlsKeyStore, keyStore, "algorithm");
        tlsContext.addContent(keyStore);
        tlsConfigured = true;
      }
      Element tlsClient = imapsConnector.get().getChild("tls-trust-store", imapsNamespace);
      if (tlsClient != null) {
        Element keyStore = new Element("trust-store", TLS_NAMESPACE);
        copyAttributeIfPresent(tlsClient, keyStore, "path");
        copyAttributeIfPresent(tlsClient, keyStore, "storePassword", "password");
        if (tlsClient.getAttribute("class") != null) {
          report.report("email.imapTlsClientClass", tlsClient, tlsClient);
        }
        copyAttributeIfPresent(tlsClient, keyStore, "type", "type");
        tlsContext.addContent(keyStore);
        tlsConfigured = true;
      }

      if (tlsConfigured) {
        getApplicationModel().addNameSpace(TLS_NAMESPACE.getPrefix(), TLS_NAMESPACE.getURI(),
                                           "http://www.mulesoft.org/schema/mule/tls/current/mule-tls.xsd");

        imapsConnection.addContent(tlsContext);
      }
    }
  }

  @Override
  protected Element createConnection() {
    return new Element("imaps-connection", EMAIL_NAMESPACE);
  }

  @Override
  protected Element getConnection(Element m4Config) {
    return m4Config.getChild("imaps-connection", EMAIL_NAMESPACE);
  }

  @Override
  protected Element getConnector(String connectorName) {
    return getApplicationModel().getNode("/*/*[namespace-uri()='" + IMAPS_NAMESPACE_URI
        + "' and (local-name()='connector' or local-name()='gmail-connector') and @name = '" + connectorName + "']");
  }

  @Override
  protected Optional<Element> getDefaultConnector() {
    return getApplicationModel()
        .getNodeOptional("/*/*[namespace-uri()='" + IMAPS_NAMESPACE_URI
            + "' and (local-name()='connector' or local-name()='gmail-connector')]");
  }

}
