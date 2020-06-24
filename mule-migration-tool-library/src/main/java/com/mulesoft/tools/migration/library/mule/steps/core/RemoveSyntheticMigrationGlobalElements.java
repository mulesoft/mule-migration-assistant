/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Remove global elements that have the migration namespace, used internally for migration.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RemoveSyntheticMigrationGlobalElements extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "//*[namespace-uri() = 'migration']";
  public static final Namespace MIGRATION_NAMESPACE = Namespace.getNamespace("migration", "migration");

  @Override
  public String getDescription() {
    return "Remove global elements keeping temporal information.";
  }

  public RemoveSyntheticMigrationGlobalElements() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    element.detach();
    element.removeNamespaceDeclaration(MIGRATION_NAMESPACE);
  }

}
