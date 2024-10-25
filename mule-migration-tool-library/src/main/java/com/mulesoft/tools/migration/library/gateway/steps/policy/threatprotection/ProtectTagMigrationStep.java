/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.threatprotection;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.JSON_THREAT_PROTECTION_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.THREAT_PROTECTION_GW_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.XML_THREAT_PROTECTION_NAMESPACE;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrate protect element
 *
 * @author Mulesoft Inc.
 */
public class ProtectTagMigrationStep extends AbstractThreatProtectionMigrationStep {

  private static final String PROTECT = "protect";
  private static final String SECURE_JSON_REQUEST = "secure-json-request";
  private static final String SECURE_XML_REQUEST = "secure-xml-request";

  private static final String THREAT_PROTECTION_POLICY_REF = "threat-protection-policy-ref";
  private static final String ON_UNACCEPTED = "onUnaccepted";
  private static final String CONFIG_REF = "config-ref";
  private static final String CONTENT_TYPE = "contentType";
  private static final String CONTENT_TYPE_VALUE = "#[attributes.headers['content-type']]";


  public ProtectTagMigrationStep() {
    super(THREAT_PROTECTION_GW_NAMESPACE, PROTECT);
  }

  private void migrateElement(Element element, String tagName, Namespace namespace, String configRefValue) {
    element.setName(tagName);
    element.setNamespace(namespace);
    element.setAttribute(CONFIG_REF, configRefValue);
    element.setAttribute(CONTENT_TYPE, CONTENT_TYPE_VALUE);
    element.removeAttribute(THREAT_PROTECTION_POLICY_REF);
    element.removeAttribute(ON_UNACCEPTED);
  }

  private Boolean isJsonPolicy(Element element, MigrationReport migrationReport) {
    Element root = element.getDocument().getRootElement();
    if (root != null) {
      for (Element e : root.getChildren()) {
        Namespace elementNamespace = e.getNamespace();
        if (elementNamespace.equals(JSON_THREAT_PROTECTION_NAMESPACE)) {
          return true;
        } else if (elementNamespace.equals(XML_THREAT_PROTECTION_NAMESPACE)) {
          return false;
        }
      }
    }
    migrationReport.report("threatProtection.unknownPolicyType", element, element);
    return null;
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    Boolean isJsonPolicy = isJsonPolicy(element, migrationReport);
    if (isJsonPolicy != null) {
      if (isJsonPolicy) {
        migrateElement(element, SECURE_JSON_REQUEST, JSON_THREAT_PROTECTION_NAMESPACE, JSON_THREAT_PROTECTION_CONFIG);
      } else {
        migrateElement(element, SECURE_XML_REQUEST, XML_THREAT_PROTECTION_NAMESPACE, XML_THREAT_PROTECTION_CONFIG);
      }
    }
  }
}
