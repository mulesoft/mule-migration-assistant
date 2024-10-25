/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.basicstructure;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_3_POLICY_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_CORE_NAMESPACE_NO_PREFIX;

import com.mulesoft.tools.migration.library.gateway.steps.policy.PolicyMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.List;

import org.jdom2.Content;
import org.jdom2.Element;

/**
 * Migrate before element
 *
 * @author Mulesoft Inc.
 */
public class BeforeTagMigrationStep extends AbstractBasicStructureMigrationStep {

  private static final String BEFORE_TAG_NAME = "before";

  public BeforeTagMigrationStep() {
    super(MULE_3_POLICY_NAMESPACE, BEFORE_TAG_NAME);
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    final List<Content> cloneContentList = detachContent(element.getContent());
    Element source = setUpHttpPolicy(element, true, migrationReport);
    if (source.getChild(PolicyMigrationStep.TRY_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX) == null) {
      source.addContent(0, cloneContentList);
      replaceNamespace(source.getContent());
    } else {
      Element tryElement = source.getChild(PolicyMigrationStep.TRY_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
      tryElement.addContent(0, cloneContentList);
      replaceNamespace(tryElement.getContent());
    }
  }
}
