/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project;

import com.mulesoft.tools.migration.project.structure.BasicProject;
import com.mulesoft.tools.migration.project.structure.JavaProject;
import com.mulesoft.tools.migration.project.structure.ProjectType;
import com.mulesoft.tools.migration.project.structure.mule.four.MuleFourApplication;
import com.mulesoft.tools.migration.project.structure.mule.four.MuleFourDomain;

import java.nio.file.Path;

import static com.mulesoft.tools.migration.project.structure.ProjectType.*;

/**
 * Based on the input project type it returns the output project
 *
 * @author Mulesoft Inc.
 */
public class ProjectMatcher {

  public static BasicProject getProjectDestination(Path outputProject, ProjectType inputProjectType) {
    if (inputProjectType.equals(MULE_THREE_APPLICATION)) {
      return new MuleFourApplication(outputProject);
    } else if (inputProjectType.equals(MULE_THREE_DOMAIN)) {
      return new MuleFourDomain(outputProject);
    } else if (inputProjectType.equals(JAVA)) {
      return new JavaProject(outputProject);
    } else {
      return new BasicProject(outputProject);
    }
  }



}
