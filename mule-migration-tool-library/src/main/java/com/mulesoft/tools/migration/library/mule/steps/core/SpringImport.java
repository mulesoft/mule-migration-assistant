/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.isMuleConfigFile;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Migrate spring:import to import for mule config files.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SpringImport extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri()='http://www.springframework.org/schema/beans' and local-name()='import']";

  @Override
  public String getDescription() {
    return "Migrate Spring Imports for mule config files.";
  }

  public SpringImport() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    Attribute resource = element.getAttribute("resource");
    Element parent = element.getParentElement();
    if (resource != null && isMuleConfigFile(resource.getValue(), getApplicationModel().getProjectBasePath())) {
      element.setNamespace(CORE_NAMESPACE);
      resource.setName("file");
      element.detach();
      addTopLevelElement(element, parent.getDocument());
    }
    if (parent.getChildren().isEmpty()) {
      parent.detach();
    }
  }


}
