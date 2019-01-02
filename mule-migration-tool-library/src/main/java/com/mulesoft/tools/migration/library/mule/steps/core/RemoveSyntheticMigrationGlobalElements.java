package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

public class RemoveSyntheticMigrationGlobalElements extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "//*[namespace-uri() = 'migration']";
  public static final Namespace MIGRATION_NAMESPACE = Namespace.getNamespace("migration");

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
