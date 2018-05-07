/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package ${groupId}.task;

import static com.mulesoft.tools.migration.util.MuleVersion.MULE_3_VERSION;
import static com.mulesoft.tools.migration.util.MuleVersion.MULE_4_VERSION;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;

import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.task.AbstractMigrationTask;
import com.mulesoft.tools.migration.task.Version;
import java.util.List;
import java.util.ArrayList;
import ${groupId}.step.${mainTaskClassName}PomContribution;
import ${groupId}.step.${mainTaskClassName}ApplicationModelContribution;

/**
 * Class documentation such as "Migration definition for HTTP component"
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ${mainTaskClassName} extends AbstractMigrationTask {

  @Override
  public String getDescription() {
    return "Description"; // Example: "Migrate HTTP Component"
  }

  @Override
  public Version getTo() {
    return MULE_4_VERSION; // The version this task is targeting
  }

  @Override
  public Version getFrom() {
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
