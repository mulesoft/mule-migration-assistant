/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.basicstructure;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_POLICY_NAMESPACE;

import com.mulesoft.tools.migration.library.gateway.steps.policy.PolicyMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Some attributes present in the original <policy/> element, such as "policy-name", are needed in steps following <policy/>
 * but do not have to be present in <mule/> element. Therefore, this step provides the opportunity for the developer to remove
 * such attributes in order to finish the migration task correctly.
 *
 * @author Mulesoft Inc.
 */
public class CleanupAttributesMigrationStep extends AbstractBasicStructureMigrationStep {

  public CleanupAttributesMigrationStep() {
    super(MULE_4_POLICY_NAMESPACE, PolicyMigrationStep.MULE_4_TAG_NAME);
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    element.removeAttribute(PolicyMigrationStep.POLICY_NAME);
  }
}
