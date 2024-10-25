/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.threatprotection;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.THREAT_PROTECTION_GW_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.XML_THREAT_PROTECTION_NAMESPACE;

import com.mulesoft.tools.migration.library.gateway.steps.policy.PolicyMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate xml-policy element
 *
 * @author Mulesoft Inc.
 */
public class XmlPolicyTagMigrationStep extends AbstractThreatProtectionMigrationStep {

  private static final String XML_POLICY = "xml-policy";
  private static final String STRUCTURE = "structure";
  private static final String VALUES = "values";
  private static final String XML_CONFIG = "xml-config";

  private static final String XML_THREAT_PROTECTION_XSI_SCHEMA_LOCATION_URI_MULE4 =
      "http://www.mulesoft.org/schema/mule/xml-threat-protection";
  private static final String XML_THREAT_PROTECTION_XSI_SCHEMA_LOCATION_URI_MULE4_XSD =
      "http://www.mulesoft.org/schema/mule/xml-threat-protection/current/mule-xml-threat-protection.xsd";

  public XmlPolicyTagMigrationStep() {
    super(THREAT_PROTECTION_GW_NAMESPACE, XML_POLICY);
  }

  private void migrateChildElement(Element element, MigrationReport migrationReport, String childElementName) {
    Element child = element.getChild(childElementName, THREAT_PROTECTION_GW_NAMESPACE);
    if (child != null) {
      child.getAttributes().forEach(attribute -> element.setAttribute(attribute.clone()));
      child.detach();
    } else {
      migrationReport.report("threatProtection.missingElement", element, element, childElementName);
    }
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    new ThreatProtectionPomContributionMigrationStep(true).execute(getApplicationModel().getPomModel().get(), migrationReport);
    replaceNamespace(element, XML_THREAT_PROTECTION_NAMESPACE, XML_THREAT_PROTECTION_XSI_SCHEMA_LOCATION_URI_MULE4,
                     XML_THREAT_PROTECTION_XSI_SCHEMA_LOCATION_URI_MULE4_XSD);
    element.setName(XML_CONFIG);
    element.setNamespace(XML_THREAT_PROTECTION_NAMESPACE);
    element.setAttribute(NAME_ATTR_NAME, XML_THREAT_PROTECTION_CONFIG);
    element.removeAttribute(PolicyMigrationStep.ID);
    migrateChildElement(element, migrationReport, STRUCTURE);
    migrateChildElement(element, migrationReport, VALUES);
    element.removeContent();
  }
}
