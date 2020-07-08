/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.basicstructure;


import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_3_POLICY_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_CORE_NAMESPACE_NO_PREFIX;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_POLICY_NAMESPACE;

import com.mulesoft.tools.migration.library.gateway.steps.policy.PolicyMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Content;
import org.jdom2.Element;

/**
 * Migrate policy element attributes
 *
 * @author Mulesoft Inc.
 */
public class PolicyTagMigrationStep extends AbstractBasicStructureMigrationStep {

  private static final String VERSION = "version";
  private static final String ONLINE = "online";
  private static final String REQUIRES_CONTRACTS = "requiresContracts";
  private static final String ORDER = "order";
  private static final String VIOLATION_CATEGORY = "violationCategory";

  private static final List<String> ATTRIBUTES_TO_REMOVE = new ArrayList<String>() {

    {
      add(VERSION);
      add(ONLINE);
      add(REQUIRES_CONTRACTS);
      add(PolicyMigrationStep.ID);
      add(ORDER);
      add(VIOLATION_CATEGORY);
    }
  };

  private static final String POLICY_XSI_SCHEMA_LOCATION_URI_MULE3 = "http://www.mulesoft.org/schema/mule/policy";
  private static final String POLICY_XSI_SCHEMA_LOCATION_URI_MULE3_XSD =
      "http://www.mulesoft.org/schema/mule/policy/current/mule-policy.xsd";
  private static final String POLICY_XSI_SCHEMA_LOCATION_URI_MULE4 = "http://www.mulesoft.org/schema/mule/core";
  private static final String POLICY_XSI_SCHEMA_LOCATION_URI_MULE4_XSD =
      "http://www.mulesoft.org/schema/mule/core/current/mule.xsd";

  public PolicyTagMigrationStep() {
    super(MULE_3_POLICY_NAMESPACE, MULE_3_TAG_NAME);
  }

  @Override
  public String getDescription() {
    return "Update policy tag in custom policies";
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    element.setName(PolicyMigrationStep.MULE_4_TAG_NAME);
    element.setNamespace(MULE_4_CORE_NAMESPACE_NO_PREFIX);
    element.addNamespaceDeclaration(MULE_4_POLICY_NAMESPACE);
    removeAttributes(ATTRIBUTES_TO_REMOVE, element);
    final List<Content> cloneContentList = detachContentClonning(element.getContent());
    replaceSchemaLocationNamespace(element, POLICY_XSI_SCHEMA_LOCATION_URI_MULE3_XSD, POLICY_XSI_SCHEMA_LOCATION_URI_MULE4_XSD,
                                   POLICY_XSI_SCHEMA_LOCATION_URI_MULE3, POLICY_XSI_SCHEMA_LOCATION_URI_MULE4);
    element.addContent(cloneContentList);
  }
}
