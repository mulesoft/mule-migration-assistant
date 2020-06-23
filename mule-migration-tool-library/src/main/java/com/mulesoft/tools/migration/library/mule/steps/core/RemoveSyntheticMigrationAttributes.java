/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static java.util.stream.Collectors.toList;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Remove isMessageSource attribute that is used for migration purposes.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RemoveSyntheticMigrationAttributes extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "//*[@*[namespace-uri() = 'migration']]";
  public static final Namespace MIGRATION_NAMESPACE = Namespace.getNamespace("migration");

  @Override
  public String getDescription() {
    return "Update Remove Session Variable namespace to compatibility.";
  }

  public RemoveSyntheticMigrationAttributes() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    element.getAttributes()
        .stream()
        .filter(att -> att.getNamespace().equals(MIGRATION_NAMESPACE))
        .collect(toList())
        .forEach(att -> att.detach());
    element.removeNamespaceDeclaration(MIGRATION_NAMESPACE);
  }
}
