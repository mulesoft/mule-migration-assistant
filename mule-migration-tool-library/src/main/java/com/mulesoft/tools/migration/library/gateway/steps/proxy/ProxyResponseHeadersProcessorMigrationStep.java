/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.proxy;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_CORE_NAMESPACE_NO_PREFIX;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_POLICY_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.PROXY_NAMESPACE;
import static java.util.Arrays.asList;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Migrate proxy response headers elements
 *
 * @author Mulesoft Inc.
 */
public class ProxyResponseHeadersProcessorMigrationStep extends ProxyHeadersProcessorMigrationStep {

  private static final String PROXY_RESPONSE_HEADERS_PROCESSOR = "com.mulesoft.gateway.extension.ProxyResponseHeadersProcessor";
  private static final String RESPONSE_HEADERS = "response-headers";
  private static final String PROXY_RESPONSE_HEADERS = "proxyResponseHeaders";

  public ProxyResponseHeadersProcessorMigrationStep() {
    this.setNamespacesContributions(asList(MULE_4_POLICY_NAMESPACE));
    this.setAppliedTo(getXPathSelector(MULE_4_CORE_NAMESPACE_NO_PREFIX, CUSTOM_PROCESSOR, CLASS,
                                       PROXY_RESPONSE_HEADERS_PROCESSOR));
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    new ProxyPomContributionMigrationStep(true).execute(getApplicationModel().getPomModel().get(), migrationReport);
    addConfigElement(element);
    element.setName(RESPONSE_HEADERS);
    element.setNamespace(PROXY_NAMESPACE);
    element.removeAttribute(CLASS);
    element.setAttribute(new Attribute(CONFIG_REF, PROXY_CONFIG));
    element.setAttribute(new Attribute(TARGET, PROXY_RESPONSE_HEADERS));
  }
}
