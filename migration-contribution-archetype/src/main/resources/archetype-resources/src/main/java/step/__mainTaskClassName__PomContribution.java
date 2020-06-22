/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package ${groupId}.step;

import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.PomContribution;

/**
 * Class documentation such as "Adds X dependency"
 */
public class ${mainTaskClassName}PomContribution implements PomContribution {

  @Override
  public String getDescription() {
    return "Description"; // Example: "Add HTTP Connector dependency.";
  }

  @Override
  public void execute(PomModel pomModel, MigrationReport report) throws RuntimeException {
    // Perform any contribution to the pom: add dependencies, plugin or repositories or change/remove/update properties
  }

}
