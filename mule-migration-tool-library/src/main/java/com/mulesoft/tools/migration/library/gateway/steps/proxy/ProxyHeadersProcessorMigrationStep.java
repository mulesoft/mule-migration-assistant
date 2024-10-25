/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.proxy;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.PROXY_NAMESPACE;

import com.mulesoft.tools.migration.library.gateway.steps.GatewayMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Common stuff to migrate proxy headers elements
 *
 * @author Mulesoft Inc.
 */
public abstract class ProxyHeadersProcessorMigrationStep extends GatewayMigrationStep {

  protected static final String CUSTOM_PROCESSOR = "custom-processor";
  protected static final String CLASS = "class";
  protected static final String PROXY_CONFIG = "proxy-config";
  protected static final String CONFIG_REF = "config-ref";
  protected static final String TARGET = "target";
  private static final String CONFIG = "config";

  private static final String PROXY_XSI_SCHEMA_LOCATION_URI_MULE4 =
      "http://www.mulesoft.org/schema/mule/proxy http://www.mulesoft.org/schema/mule/proxy/current/mule-proxy.xsd";

  public ProxyHeadersProcessorMigrationStep() {

  }

  private Element getConfigElement() {
    return new Element(CONFIG, PROXY_NAMESPACE).setAttribute(NAME_ATTR_NAME, PROXY_CONFIG);
  }

  protected void addConfigElement(Element element, MigrationReport report) {
    Element root = getRootElement(element);
    if (root != null && root.getChild(CONFIG, PROXY_NAMESPACE) == null) {
      addNamespaceDeclaration(root, PROXY_NAMESPACE, PROXY_XSI_SCHEMA_LOCATION_URI_MULE4);
      Element configElement = getConfigElement();
      report.report("proxy.templates", element, configElement);
      root.addContent(0, configElement);
    }
  }

}
