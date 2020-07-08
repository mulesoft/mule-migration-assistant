/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.proxy;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.EXPRESSION_LANGUAGE_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_CORE_NAMESPACE_NO_PREFIX;

import com.mulesoft.tools.migration.library.gateway.steps.GatewayMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate properties placeholders
 *
 * @author Mulesoft Inc.
 */
public class PropertyPlaceholderTagMigrationStep extends GatewayMigrationStep {

  private static final String CONFIGURATION_PROPERTIES_TAG_NAME = "configuration-properties";
  private static final String PROPERTY_PLACEHOLDER_TAG_NAME = "property-placeholder";

  private static final String LOCATION = "location";
  private static final String FILE = "file";

  private static final String EXPRESSION_LANGUAGE_XSI_SCHEMA_LOCATION_URI_MULE3 =
      "http://www.mulesoft.org/schema/mule/expression-language-gw";
  private static final String EXPRESSION_LANGUAGE_XSI_SCHEMA_LOCATION_URI_MULE3_XSD =
      "http://www.mulesoft.org/schema/mule/expression-language-gw/current/mule-expression-language-gw.xsd";
  private static final String POLICY_XSI_SCHEMA_LOCATION_URI_MULE4 = "http://www.mulesoft.org/schema/mule/core";
  private static final String POLICY_XSI_SCHEMA_LOCATION_URI_MULE4_XSD =
      "http://www.mulesoft.org/schema/mule/core/current/mule.xsd";

  public PropertyPlaceholderTagMigrationStep() {
    super(EXPRESSION_LANGUAGE_NAMESPACE, PROPERTY_PLACEHOLDER_TAG_NAME);
  }


  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    Element root = getRootElement(element);
    if (root != null) {
      root.removeNamespaceDeclaration(EXPRESSION_LANGUAGE_NAMESPACE);
      removeSchemaLocationNamespace(root, EXPRESSION_LANGUAGE_XSI_SCHEMA_LOCATION_URI_MULE3_XSD,
                                    POLICY_XSI_SCHEMA_LOCATION_URI_MULE4_XSD);
      removeSchemaLocationNamespace(root, EXPRESSION_LANGUAGE_XSI_SCHEMA_LOCATION_URI_MULE3,
                                    POLICY_XSI_SCHEMA_LOCATION_URI_MULE4);
    }
    element.setName(CONFIGURATION_PROPERTIES_TAG_NAME);
    element.setNamespace(MULE_4_CORE_NAMESPACE_NO_PREFIX);
    element.getAttribute(LOCATION).setName(FILE);
    element.removeContent();
  }
}
