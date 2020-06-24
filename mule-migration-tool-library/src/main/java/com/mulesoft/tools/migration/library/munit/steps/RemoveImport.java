/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.munit.steps;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.util.XmlDslUtils;

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

  private static final String MUNIT_PATH = "src/test/munit";
  private static final String XPATH_SELECTOR = getCoreXPathSelector("import");

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
