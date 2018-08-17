/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Attribute;
import org.jdom2.Element;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.isMuleConfigFile;

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
