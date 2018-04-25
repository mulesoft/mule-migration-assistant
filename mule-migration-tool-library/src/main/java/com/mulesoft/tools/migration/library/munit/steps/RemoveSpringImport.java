/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.munit.steps;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Remove spring beans import since no longer needed on MUnit 2
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RemoveSpringImport extends AbstractApplicationModelMigrationStep {

  private static final String MUNIT_PATH = "src/test/munit";
  private static final String SPRING_NAME = "spring";
  private static final String SPRING_URI = "http://www.springframework.org/schema/beans";
  private static final String XPATH_SELECTOR = "//*[local-name()='beans']";

  @Override
  public String getDescription() {
    return "Remove spring beans.";
  }

  public RemoveSpringImport() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    //TODO MMT-99 Once spring migration is supported, need to update this to only remove mule config files
    if (isMUnitFile(element.getDocument())) {
      removeSpringNamespace(element.getDocument());
      element.detach();
    }
  }

  private void removeSpringNamespace(Document document) {
    document.getRootElement().removeNamespaceDeclaration(Namespace.getNamespace(SPRING_NAME, SPRING_URI));
  }

  public boolean isMUnitFile(Document document) {
    return document.getBaseURI().contains(MUNIT_PATH);
  }
}
