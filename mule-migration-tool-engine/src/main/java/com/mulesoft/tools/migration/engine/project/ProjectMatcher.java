/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine.project;

import static com.mulesoft.tools.migration.project.ProjectType.JAVA;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_DOMAIN;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_THREE_APPLICATION;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_THREE_DOMAIN;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_THREE_MAVEN_APPLICATION;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_THREE_MAVEN_DOMAIN;
import static com.mulesoft.tools.migration.project.ProjectType.MULE_THREE_POLICY;

import com.mulesoft.tools.migration.engine.project.structure.BasicProject;
import com.mulesoft.tools.migration.engine.project.structure.JavaProject;
import com.mulesoft.tools.migration.engine.project.structure.mule.four.MuleFourApplication;
import com.mulesoft.tools.migration.engine.project.structure.mule.four.MuleFourDomain;
import com.mulesoft.tools.migration.engine.project.structure.mule.four.MuleFourPolicy;
import com.mulesoft.tools.migration.project.ProjectType;

import java.nio.file.Path;

/**
 * Based on the input project type it returns the output project
 *
 * @author Mulesoft Inc.
 */
public class ProjectMatcher {

  public static BasicProject getProjectDestination(Path outputProject, ProjectType inputProjectType) {
    if (inputProjectType.equals(MULE_THREE_APPLICATION)
        || inputProjectType.equals(MULE_THREE_MAVEN_APPLICATION)
        || inputProjectType.equals(MULE_FOUR_APPLICATION)) {
      return new MuleFourApplication(outputProject);
    } else if (inputProjectType.equals(MULE_THREE_DOMAIN)
        || inputProjectType.equals(MULE_THREE_MAVEN_DOMAIN)
        || inputProjectType.equals(MULE_FOUR_DOMAIN)) {
      return new MuleFourDomain(outputProject);
    } else if (inputProjectType.equals(MULE_THREE_POLICY)) {
      return new MuleFourPolicy(outputProject);
    } else if (inputProjectType.equals(JAVA)) {
      return new JavaProject(outputProject);
    } else {
      return new BasicProject(outputProject);
    }
  }
}
