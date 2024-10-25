/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
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
