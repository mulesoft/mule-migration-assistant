/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.threatprotection;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.THREAT_PROTECTION_GW_NAMESPACE;

import com.mulesoft.tools.migration.library.gateway.steps.policy.PolicyMigrationStep;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Common stuff to migrate threat protection elements
 *
 * @author Mulesoft Inc.
 */
public abstract class AbstractThreatProtectionMigrationStep extends PolicyMigrationStep {

  protected static final String JSON_THREAT_PROTECTION_CONFIG = "json-threat-protection-config";
  protected static final String XML_THREAT_PROTECTION_CONFIG = "xml-threat-protection-config";

  private static final String THREAT_PROTECTION_XSI_SCHEMA_LOCATION_URI_MULE3 =
      "http://www.mulesoft.org/schema/mule/threat-protection-gw";
  private static final String THREAT_PROTECTION_XSI_SCHEMA_LOCATION_URI_MULE3_XSD =
      "http://www.mulesoft.org/schema/mule/threat-protection-gw/current/mule-threat-protection-gw.xsd";

  public AbstractThreatProtectionMigrationStep(Namespace namespace, String tagName) {
    super(namespace, tagName);
  }

  protected void replaceNamespace(Element element, Namespace mule4Namespace, String mule4SchemaLocationUri,
                                  String mule4SchemaLocationUriXsd) {
    Element root = getRootElement(element);
    if (root != null) {
      root.addNamespaceDeclaration(mule4Namespace);
      root.removeNamespaceDeclaration(THREAT_PROTECTION_GW_NAMESPACE);
      replaceSchemaLocationNamespace(root, THREAT_PROTECTION_XSI_SCHEMA_LOCATION_URI_MULE3_XSD,
                                     mule4SchemaLocationUriXsd, THREAT_PROTECTION_XSI_SCHEMA_LOCATION_URI_MULE3,
                                     mule4SchemaLocationUri);
    }
  }
}
