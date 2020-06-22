/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package ${groupId}.step;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Class documentation such as "Migrates X component"
 */
public class ${mainTaskClassName}ApplicationModelContribution extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "xpath query to select the element to migrate"; // Example: "/*/http:listener-config"

  @Override
  public String getDescription() {
    return "Description"; // Example: "Update HTTP Connector listener config"
  }

  public ${mainTaskClassName}ApplicationModelContribution() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    // Perform here all the necessary changes in the element
  }

}
