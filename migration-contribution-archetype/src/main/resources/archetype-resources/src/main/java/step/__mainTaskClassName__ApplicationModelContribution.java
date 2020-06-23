/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
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
