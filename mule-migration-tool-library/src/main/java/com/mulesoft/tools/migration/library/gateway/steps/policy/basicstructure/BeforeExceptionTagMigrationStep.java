/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.basicstructure;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_3_POLICY_NAMESPACE;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate before-exception element
 *
 * @author Mulesoft Inc.
 */
public class BeforeExceptionTagMigrationStep extends AbstractBasicStructureMigrationStep {

  private static final String BEFORE_EXCEPTION_TAG_NAME = "before-exception";

  public BeforeExceptionTagMigrationStep() {
    super(MULE_3_POLICY_NAMESPACE, BEFORE_EXCEPTION_TAG_NAME);
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    migrationReport.report("basicStructure.beforeExceptionMigrationStep", element, element);
    detachContent(element.getContent());
    setUpHttpPolicy(element, true, migrationReport);
  }
}
