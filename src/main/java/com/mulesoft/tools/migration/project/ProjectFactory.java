/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project;

import com.mulesoft.tools.migration.project.structure.JavaProject;
import com.mulesoft.tools.migration.project.structure.ProjectType;
import com.mulesoft.tools.migration.project.structure.mule.four.MuleApplication;
import com.mulesoft.tools.migration.project.structure.mule.three.MuleDomain;
import com.mulesoft.tools.migration.project.structure.mule.three.MuleApplicationProject;

import java.nio.file.Path;

import static com.mulesoft.tools.migration.project.structure.ProjectType.*;
import static java.nio.file.Files.exists;

/**
 * It gets the project type based on the project path
 *
 * @author Mulesoft Inc.
 */
public class ProjectFactory {

  public ProjectType getProjectType(Path projectPath) throws Exception {
    if (exists(projectPath.resolve(MuleApplication.srcMainConfigurationPath))) {
      return MULE_APPLICATION;
    } else if (exists(projectPath.resolve(MuleDomain.srcMainConfigurationPath))) {
      return MULE_DOMAIN;
    } else if (exists(projectPath.resolve(MuleApplicationProject.srcMainConfigurationPath))) {
      return MULE;
    } else if (exists(projectPath.resolve(JavaProject.srcMainJavaPath))) {
      return JAVA;
    } else {
      return BASIC;
    }
  }
}
