/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;


import static com.mulesoft.tools.migration.library.mule.steps.http.HttpsOutboundEndpoint.migrate;
import static java.util.Optional.of;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrates the polling connector of the https transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class HttpsPollingConnector extends HttpPollingConnector {

  private static final String HTTP_NS_PREFIX = "http";
  private static final String HTTP_NS_URI = "http://www.mulesoft.org/schema/mule/http";
  public static final String XPATH_SELECTOR = "/mule:mule/https:polling-connector";

  @Override
  public String getDescription() {
    return "Update HTTPs polling connector.";
  }

  public HttpsPollingConnector() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    super.execute(object, report);
    getApplicationModel().addNameSpace(HTTP_NS_PREFIX, HTTP_NS_URI,
                                       "http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd");

    Element httpsRequesterConnection = getApplicationModel().getNode("/mule:mule/http:request-config[@name = '"
        + object.getAttributeValue("name") + "Config']/http:request-connection");

    HttpsOutboundEndpoint httpRequesterMigrator = new HttpsOutboundEndpoint();
    httpRequesterMigrator.setApplicationModel(getApplicationModel());
    migrate(httpsRequesterConnection, of(object), report, getApplicationModel(), "tls-server");
  }

}
