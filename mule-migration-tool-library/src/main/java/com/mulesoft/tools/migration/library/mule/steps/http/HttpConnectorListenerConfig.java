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
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.copyAttributeIfPresent;
import static java.util.Arrays.asList;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrates the listener configuration of the HTTP Connector
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpConnectorListenerConfig extends AbstractHttpConnectorMigrationStep {

  public static final String XPATH_SELECTOR =
      "/*/*[namespace-uri()='" + HTTP_NAMESPACE_URI + "' and local-name()='listener-config']";

  @Override
  public String getDescription() {
    return "Update HTTP Connector listener config.";
  }

  public HttpConnectorListenerConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(asList(HTTP_NAMESPACE));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.setNamespace(HTTP_NAMESPACE);

    if ("listener-config".equals(object.getName()) && object.getChild("listener-connection", HTTP_NAMESPACE) == null) {
      final Element listenerConnection = new Element("listener-connection", HTTP_NAMESPACE);
      copyAttributeIfPresent(object, listenerConnection, "protocol");
      copyAttributeIfPresent(object, listenerConnection, "host");
      copyAttributeIfPresent(object, listenerConnection, "port");
      copyAttributeIfPresent(object, listenerConnection, "usePersistentConnections");
      copyAttributeIfPresent(object, listenerConnection, "connectionIdleTimeout");
      copyAttributeIfPresent(object, listenerConnection, "tlsContext-ref", "tlsContext");

      if (object.getAttribute("parseRequest") != null && !"false".equals(object.getAttributeValue("parseRequest"))) {
        report.report("http.parseRequest", object, object);
      }
      object.addContent(listenerConnection);
    }


    object.getChildren().forEach(c -> {
      if (HTTP_NAMESPACE_URI.equals(c.getNamespaceURI())) {
        execute(c, report);
      } else if (TLS_NAMESPACE_URI.equals(c.getNamespaceURI()) && "context".equals(c.getName())) {
        final Element listenerConnection = c.getParentElement().getChild("listener-connection", HTTP_NAMESPACE);
        c.getParentElement().removeContent(c);
        listenerConnection.addContent(c);
      }
    });

    if ("worker-threading-profile".equals(object.getName())) {
      report.report("flow.threading", object, object.getParentElement());
      object.getParentElement().removeContent(object);
    }
  }

}
