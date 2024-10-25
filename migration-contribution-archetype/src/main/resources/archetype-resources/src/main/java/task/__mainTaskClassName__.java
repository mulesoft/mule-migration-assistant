/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package ${groupId}.task;

import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;

import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;
import java.util.List;
import java.util.ArrayList;
import ${groupId}.step.${mainTaskClassName}PomContribution;
import ${groupId}.step.${mainTaskClassName}ApplicationModelContribution;

/**
 * Class documentation such as "Migration definition for X component"
 */
public class ${mainTaskClassName} extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Description"; // Example: "Migrate HTTP Component"
  }

  @Override
  public String getTo() {
    return MULE_4_VERSION; // The version this task is targeting
  }

  @Override
  public String getFrom() {
    return MULE_3_VERSION; // The version this task is starting from
  }

  @Override
  public ProjectType getProjectType() {
    return MULE_FOUR_APPLICATION; // The project type this task can work over, such as application or domains
  }

  @Override
  public List<MigrationStep> getSteps() {
    List<MigrationStep> steps = new ArrayList<>();

    steps.add(new ${mainTaskClassName}PomContribution());
    steps.add(new ${mainTaskClassName}ApplicationModelContribution());
    // Add as many steps as necessary
    return steps;
  }
}
