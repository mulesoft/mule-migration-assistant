/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.clientidenforcement;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.API_PLATFORM_GW_MULE_3_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.CLIENT_ID_ENFORCEMENT_NAMESPACE;

import com.mulesoft.tools.migration.library.gateway.steps.policy.FilterTagMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate validate client element
 *
 * @author Mulesoft Inc.
 */
public class ValidateClientTagMigrationStep extends FilterTagMigrationStep {

  private static final String VALIDATE_CLIENT_TAG_NAME = "validate-client";
  private static final String VALIDATE_CLIENT_ID_TAG_NAME = "validate-client-id";
  private static final String VALIDATE_BASIC_AUTH_ENCODED_CLIENT_TAG_NAME = "validate-basic-auth-encoded-client";
  private static final String BASIC_AUTH_ENABLED_ATTR_NAME = "basicAuthEnabled";
  private static final String CLIENT_ID_ATTR_NAME = "clientId";
  private static final String CLIENT_SECRET_ATTR_NAME = "clientSecret";
  private static final String CONFIG = "config";
  private static final String CLIENT_ID_ENFORCEMENT_XSI_SCHEMA_LOCATION_URI_MULE4 =
      "http://www.mulesoft.org/schema/mule/client-id-enforcement http://www.mulesoft.org/schema/mule/client-id-enforcement/current/mule-client-id-enforcement.xsd";

  private static final String ON_UNACCEPTED_ATTR_NAME = "onUnaccepted";
  private static final String CONFIG_REF = "config-ref";
  private static final String CLIENT_ID_ENFORCEMENT_CONFIG = "clientEnforcementConfig";
  private static final String ENCODED_CLIENT = "encodedClient";
  private static final String ENCODED_CLIENT_VALUE = "#[attributes.headers.authorization]";
  private static final String TRUE = "true";

  public ValidateClientTagMigrationStep() {
    super(API_PLATFORM_GW_MULE_3_NAMESPACE, VALIDATE_CLIENT_TAG_NAME);
  }

  private void addConfigElement(Element root) {
    if (root != null && root.getChild(CONFIG, CLIENT_ID_ENFORCEMENT_NAMESPACE) == null) {
      addNamespaceDeclaration(root, CLIENT_ID_ENFORCEMENT_NAMESPACE, CLIENT_ID_ENFORCEMENT_XSI_SCHEMA_LOCATION_URI_MULE4);
      root.addContent(0, new Element(CONFIG, CLIENT_ID_ENFORCEMENT_NAMESPACE).setAttribute(NAME_ATTR_NAME,
                                                                                           CLIENT_ID_ENFORCEMENT_CONFIG));
    }
  }

  private void onValidMigrationElement(Element element, MigrationReport migrationReport) {
    element.setNamespace(CLIENT_ID_ENFORCEMENT_NAMESPACE);
    Element root = getRootElement(element);
    addConfigElement(root);
    final String onUnacceptedName = element.getAttributeValue(ON_UNACCEPTED_ATTR_NAME);
    if (root != null && hasProcessorChain(root, onUnacceptedName)) {
      migrateProcessorChain(root, onUnacceptedName, migrationReport);
    }
    element.removeAttribute(ON_UNACCEPTED_ATTR_NAME);
    element.setAttribute(CONFIG_REF, CLIENT_ID_ENFORCEMENT_CONFIG);
  }

  private void migrateNonBasicAuthElement(Element element, MigrationReport migrationReport) {
    if (element.getAttribute(CLIENT_SECRET_ATTR_NAME) != null) {
      onValidMigrationElement(element, migrationReport);
    } else if (element.getAttribute(CLIENT_ID_ATTR_NAME) != null) {
      element.setName(VALIDATE_CLIENT_ID_TAG_NAME);
      onValidMigrationElement(element, migrationReport);
    } else {
      migrationReport.report("clientIdEnforcement.invalidMigrationElement", element, element);
    }
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    if (element.getAttribute(BASIC_AUTH_ENABLED_ATTR_NAME) != null) {
      if (element.getAttributeValue(BASIC_AUTH_ENABLED_ATTR_NAME).equals(TRUE)) {
        element.setName(VALIDATE_BASIC_AUTH_ENCODED_CLIENT_TAG_NAME);
        element.removeAttribute(BASIC_AUTH_ENABLED_ATTR_NAME);
        element.setAttribute(ENCODED_CLIENT, ENCODED_CLIENT_VALUE);
        onValidMigrationElement(element, migrationReport);
      } else {
        element.removeAttribute(BASIC_AUTH_ENABLED_ATTR_NAME);
        migrateNonBasicAuthElement(element, migrationReport);
      }
    } else {
      migrateNonBasicAuthElement(element, migrationReport);
    }
  }

  @Override
  protected void migrateProcessorChain(Element root, String onUnacceptedName, MigrationReport migrationReport) {
    ValidateClientProcessorChainTagMigrationStep step = new ValidateClientProcessorChainTagMigrationStep(onUnacceptedName);
    step.setApplicationModel(getApplicationModel());
    step.execute(getProcessorChain(root, onUnacceptedName),
                 migrationReport);
  }
}
