/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;


import static com.mulesoft.tools.migration.library.mule.steps.http.AbstractHttpConnectorMigrationStep.HTTPS_NAMESPACE_URI;
import static com.mulesoft.tools.migration.library.mule.steps.http.AbstractHttpConnectorMigrationStep.HTTP_NAMESPACE_URI;
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

  public static final String XPATH_SELECTOR =
      "/*/*[namespace-uri()='" + HTTPS_NAMESPACE_URI + "' and local-name()='polling-connector']";

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

    Element httpsRequesterConnection = getApplicationModel()
        .getNode("/*/*[namespace-uri()='" + HTTP_NAMESPACE_URI + "' and local-name()='request-config' and @name = '"
            + object.getAttributeValue("name") + "Config']/*[namespace-uri()='" + HTTP_NAMESPACE_URI
            + "' and local-name()='request-connection']");

    HttpsOutboundEndpoint httpRequesterMigrator = new HttpsOutboundEndpoint();
    httpRequesterMigrator.setApplicationModel(getApplicationModel());
    migrate(httpsRequesterConnection, of(object), report, getApplicationModel(), "tls-server");
  }

}
