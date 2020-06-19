/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
