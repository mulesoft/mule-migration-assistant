/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine.project;

import static com.mulesoft.tools.migration.project.ProjectType.BASIC;
import static com.mulesoft.tools.migration.project.ProjectType.JAVA;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_THREE_APPLICATION;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_THREE_DOMAIN;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_THREE_MAVEN_APPLICATION;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_THREE_MAVEN_DOMAIN;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_THREE_POLICY;

import com.mulesoft.tools.migration.engine.exception.MigrationJobException;
import com.mulesoft.tools.migration.engine.project.structure.mule.MuleProject;
import com.mulesoft.tools.migration.engine.project.structure.mule.three.MuleThreeApplication;
import com.mulesoft.tools.migration.engine.project.structure.mule.three.MuleThreeDomain;
import com.mulesoft.tools.migration.engine.project.structure.mule.three.MuleThreeMavenApplication;
import com.mulesoft.tools.migration.engine.project.structure.mule.three.MuleThreeMavenDomain;
import com.mulesoft.tools.migration.engine.project.structure.mule.three.MuleThreePolicy;
import com.mulesoft.tools.migration.project.ProjectType;

import java.nio.file.Path;

/**
 * Generates an {@link MuleProject} for a valid mule project
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MuleProjectFactory {

  public static MuleProject getMuleProject(Path projectPath, ProjectType projectType) throws Exception {
    if (!projectType.equals(BASIC) && !projectType.equals(JAVA)) {
      if (projectType.equals(MULE_THREE_APPLICATION)) {
        return new MuleThreeApplication(projectPath);
      } else if (projectType.equals(MULE_THREE_MAVEN_APPLICATION)) {
        return new MuleThreeMavenApplication(projectPath);
      } else if (projectType.equals(MULE_THREE_DOMAIN)) {
        return new MuleThreeDomain(projectPath);
      } else if (projectType.equals(MULE_THREE_MAVEN_DOMAIN)) {
        return new MuleThreeMavenDomain(projectPath);
      } else if (projectType.equals(MULE_THREE_POLICY)) {
        return new MuleThreePolicy(projectPath);
      }
    }
    throw new MigrationJobException("Cannot read mule project. Is it a Mule Studio project?");
  }
}
