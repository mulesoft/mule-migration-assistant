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

import java.io.File;

/**
 * Remove spring beans import since no longer needed on MUnit 2
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class RemoveImport extends AbstractApplicationModelMigrationStep {

  private static final String MUNIT_PATH = "src" + File.separator + "test" + File.separator + "munit";
  private static final String XPATH_SELECTOR = "//*[local-name()='import']";

  @Override
  public String getDescription() {
    return "Remove spring beans.";
  }

  public RemoveImport() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    if (isMUnitFile(element.getDocument())) {
      element.detach();
    }
  }

  public boolean isMUnitFile(Document document) {
    return document.getBaseURI() != null && document.getBaseURI().contains(MUNIT_PATH);
  }
}
