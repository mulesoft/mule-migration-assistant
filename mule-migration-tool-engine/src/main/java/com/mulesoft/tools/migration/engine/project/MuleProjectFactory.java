/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine.project;

import com.mulesoft.tools.migration.engine.exception.MigrationJobException;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.engine.project.structure.mule.MuleProject;
import com.mulesoft.tools.migration.engine.project.structure.mule.three.MuleThreeApplication;
import com.mulesoft.tools.migration.engine.project.structure.mule.three.MuleThreeDomain;
import com.mulesoft.tools.migration.engine.project.structure.mule.three.MuleThreeMavenApplication;
import com.mulesoft.tools.migration.engine.project.structure.mule.three.MuleThreeMavenDomain;

import java.nio.file.Path;

import static com.mulesoft.tools.migration.project.ProjectType.*;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_THREE_DOMAIN;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_THREE_MAVEN_DOMAIN;

/**
 * Generates an {@link MuleProject} for a valid mule project
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MuleProjectFactory {

  private static ProjectTypeFactory projectFactory = new ProjectTypeFactory();

  public static MuleProject getMuleProject(Path projectPath) throws Exception {
    ProjectType type = projectFactory.getProjectType(projectPath);
    if (!type.equals(BASIC) && !type.equals(JAVA)) {
      if (type.equals(MULE_THREE_APPLICATION)) {
        return new MuleThreeApplication(projectPath);
      } else if (type.equals(MULE_THREE_MAVEN_APPLICATION)) {
        return new MuleThreeMavenApplication(projectPath);
      } else if (type.equals(MULE_THREE_DOMAIN)) {
        return new MuleThreeDomain(projectPath);
      } else if (type.equals(MULE_THREE_MAVEN_DOMAIN)) {
        return new MuleThreeMavenDomain(projectPath);
      }
    }
    throw new MigrationJobException("Cannot generate mule project");
  }
}
