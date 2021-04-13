/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.proxy.raml;

import static com.mulesoft.tools.migration.library.gateway.steps.ElementFinder.findChildElement;
import static com.mulesoft.tools.migration.library.gateway.steps.ElementFinder.findChildElementWithMatchingAttributeValue;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.APIKIT_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_CORE_NAMESPACE_NO_PREFIX;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.REST_VALIDATOR_NAMESPACE;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate console tag
 *
 * @author Mulesoft Inc.
 */
public class ConsoleTagMigrationStep extends RamlMigrationStep {

  private static final String CONSOLE_TAG_NAME = "console";
  private static final String FLOW_TAG_NAME = "flow";

  private static final String VALIDATE_REQUEST_TAG_NAME = "validate-request";

  public ConsoleTagMigrationStep() {
    super(APIKIT_NAMESPACE, CONSOLE_TAG_NAME);
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    String configRef = element.getAttributeValue(CONFIG_REF_ATTR_NAME);
    if (hasMatchingRamlProxyConfig(getRootElement(element), configRef)) {
      element.setNamespace(REST_VALIDATOR_NAMESPACE);
    }
  }

  private boolean hasMatchingRamlProxyConfig(Element rootElement, String configRefValue) {
    return findChildElement(rootElement, FLOW_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX,
                            flowElement -> findChildElementWithMatchingAttributeValue(flowElement, VALIDATE_REQUEST_TAG_NAME,
                                                                                      REST_VALIDATOR_NAMESPACE,
                                                                                      CONFIG_REF_ATTR_NAME, configRefValue)
                                                                                          .isPresent()).isPresent();
  }

}
