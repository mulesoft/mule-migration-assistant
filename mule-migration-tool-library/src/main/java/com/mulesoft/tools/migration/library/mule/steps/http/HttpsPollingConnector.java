/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static java.util.Optional.of;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrates the polling connector of the https transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpsPollingConnector extends HttpPollingConnector {

  public static final String XPATH_SELECTOR = "/mule:mule/https:polling-connector";

  private HttpsOutboundEndpoint httpRequesterMigrator = new HttpsOutboundEndpoint();

  @Override
  public String getDescription() {
    return "Update HTTPs polling connector.";
  }

  public HttpsPollingConnector() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Namespace httpsNamespace = Namespace.getNamespace("https", "http://www.mulesoft.org/schema/mule/https");
    Namespace tlsNamespace = Namespace.getNamespace("tls", "http://www.mulesoft.org/schema/mule/tls");

    super.execute(object, report);

    Element httpsRequesterConnection = getApplicationModel().getNode("/mule:mule/http:request-config[@name = '"
        + object.getAttributeValue("name") + "Config']/http:request-connection");

    httpRequesterMigrator.migrate(httpsRequesterConnection, of(object), report, httpsNamespace, tlsNamespace);
  }

}
