/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.munit.steps;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.NamespaceContribution;

import org.jdom2.Document;
import org.jdom2.Namespace;

/**
 * This steps migrates the MUnit 1.x assert-true
 *
 * @author Mulesoft Inc.
 */
public class MUnitNamespaces implements NamespaceContribution {

  private static final String MUNIT_PATH = "src/test/munit";
  private static final String MUNIT_MOCK_NAME = "mock";
  public static final String MUNIT_MOCK_URI = "http://www.mulesoft.org/schema/mule/mock";
  private static final String MUNIT_MOCK_SCHEMA = "http://www.mulesoft.org/schema/mule/mock/current/mule-mock.xsd";
  private static final String MUNIT_TOOLS_NAME = "munit-tools";
  public static final String MUNIT_TOOLS_URI = "http://www.mulesoft.org/schema/mule/munit-tools";
  private static final String MUNIT_TOOLS_SCHEMA = "http://www.mulesoft.org/schema/mule/munit-tools/current/mule-munit-tools.xsd";

  @Override
  public String getDescription() {
    return "Remove MUnit Mock namespace and add MUnit-Tools default namespace.";
  }

  @Override
  public void execute(ApplicationModel applicationModel, MigrationReport report) throws RuntimeException {
    try {
      applicationModel.removeNameSpace(MUNIT_MOCK_NAME, MUNIT_MOCK_URI, MUNIT_MOCK_SCHEMA);

      Namespace namespace = Namespace.getNamespace(MUNIT_TOOLS_NAME, MUNIT_TOOLS_URI);
      applicationModel.getApplicationDocuments().values().stream().filter(d -> isMUnitFile(d))
          .forEach(e -> applicationModel.addNameSpace(namespace, MUNIT_TOOLS_SCHEMA, e));
    } catch (Exception e) {
      throw new MigrationStepException("Fail to apply step. " + e.getMessage(), e);
    }
  }

  public boolean isMUnitFile(Document document) {
    if (document.getBaseURI() != null && document.getBaseURI().contains(MUNIT_PATH)) {
      return true;
    } else {
      return false;
    }
  }
}
