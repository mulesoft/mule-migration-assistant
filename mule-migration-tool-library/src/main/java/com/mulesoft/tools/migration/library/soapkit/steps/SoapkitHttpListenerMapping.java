/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.soapkit.steps;

import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Element;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.isTopLevelElement;
import static java.util.Arrays.asList;

/**
 * Migrates http mappings made by APIkit
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SoapkitHttpListenerMapping extends AbstractSoapkitMigrationStep {

  private static final String XPATH_SELECTOR = "//*[local-name()='router' and namespace-uri()='" + SOAPKIT_NAMESPACE_URI + "']";
  private static final String HTTP_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/http";

  @Override
  public String getDescription() {
    return "Update APIkit Http Listener Mappings";
  }

  public SoapkitHttpListenerMapping() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(asList(SOAPKIT_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    final Element flow = getParentFlow(element);

    if (flow != null) {
      final Element httpListener = getHttpListener(flow);
      migrateResponse(httpListener.getChild("response", httpListener.getNamespace()));
      migrateResponse(httpListener.getChild("error-response", httpListener.getNamespace()));
    }
  }

  private void migrateResponse(Element response) {
    migrateBodyMapping(response);
    migrateHeadersMapping(response);
  }

  private void migrateHeadersMapping(Element response) {
    Element header = response.getChild("headers", response.getNamespace());
    if (header == null) {
      header = new Element("headers", response.getNamespace());
      response.addContent(header);
    }
    header.setText("#[attributes.protocolHeaders default {}]");
  }

  private void migrateBodyMapping(Element response) {
    Element header = response.getChild("body", response.getNamespace());
    if (header == null) {
      header = new Element("body", response.getNamespace());
      response.addContent(0, header);
    }
    header.setText("#[payload]");
  }

  private static boolean isHttpListener(Element element) {
    return HTTP_NAMESPACE_URI.equals(element.getNamespace().getURI()) && "listener".equalsIgnoreCase(element.getName());
  }

  private static Element getHttpListener(Element flow) {
    return flow.getChildren().stream().filter(SoapkitHttpListenerMapping::isHttpListener).findFirst().orElse(null);
  }

  private static Element getParentFlow(Element element) {
    if (isTopLevelElement(element))
      return null;

    final Element parent = element.getParentElement();

    if ("flow".equalsIgnoreCase(parent.getName()))
      return parent;

    return getParentFlow(element);
  }

}
