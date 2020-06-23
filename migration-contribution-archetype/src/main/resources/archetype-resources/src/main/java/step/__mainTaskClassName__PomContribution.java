/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
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
