/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.threatprotection;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.JSON_THREAT_PROTECTION_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.THREAT_PROTECTION_GW_NAMESPACE;

import com.mulesoft.tools.migration.library.gateway.steps.policy.PolicyMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate json-policy element
 *
 * @author Mulesoft Inc.
 */
public class JsonPolicyTagMigrationStep extends AbstractThreatProtectionMigrationStep {

  private static final String JSON_POLICY = "json-policy";
  private static final String JSON_CONFIG = "json-config";

  private static final String JSON_THREAT_PROTECTION_XSI_SCHEMA_LOCATION_URI_MULE4 =
      "http://www.mulesoft.org/schema/mule/json-threat-protection";
  private static final String JSON_THREAT_PROTECTION_XSI_SCHEMA_LOCATION_URI_MULE4_XSD =
      "http://www.mulesoft.org/schema/mule/json-threat-protection/current/mule-json-threat-protection.xsd";

  public JsonPolicyTagMigrationStep() {
    super(THREAT_PROTECTION_GW_NAMESPACE, JSON_POLICY);
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    new ThreatProtectionPomContributionMigrationStep(false).execute(getApplicationModel().getPomModel().get(), migrationReport);
    replaceNamespace(element, JSON_THREAT_PROTECTION_NAMESPACE, JSON_THREAT_PROTECTION_XSI_SCHEMA_LOCATION_URI_MULE4,
                     JSON_THREAT_PROTECTION_XSI_SCHEMA_LOCATION_URI_MULE4_XSD);
    element.setName(JSON_CONFIG);
    element.setNamespace(JSON_THREAT_PROTECTION_NAMESPACE);
    element.setAttribute(NAME_ATTR_NAME, JSON_THREAT_PROTECTION_CONFIG);
    element.removeAttribute(PolicyMigrationStep.ID);
  }

}
