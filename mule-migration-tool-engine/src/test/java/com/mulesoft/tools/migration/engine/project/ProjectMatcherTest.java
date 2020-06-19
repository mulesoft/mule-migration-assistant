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

import com.mulesoft.tools.migration.engine.project.structure.BasicProject;
import com.mulesoft.tools.migration.engine.project.structure.JavaProject;
import com.mulesoft.tools.migration.engine.project.structure.mule.four.MuleFourApplication;
import com.mulesoft.tools.migration.engine.project.structure.mule.four.MuleFourDomain;
import com.mulesoft.tools.migration.project.ProjectType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;

import static com.mulesoft.tools.migration.engine.project.ProjectMatcher.getProjectDestination;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

/**
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ProjectMatcherTest {

  private static final String APP_NAME = "test-app";
  private Path projectPath;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void setUp() throws Exception {
    projectPath = temporaryFolder.newFolder(APP_NAME).toPath();
  }

  @Test
  public void getProjectMatchBasic() throws Exception {
    assertThat("The expected project type is not the same",
               getProjectDestination(projectPath, ProjectType.BASIC),
               instanceOf(BasicProject.class));
  }

  @Test
  public void getProjectMatchJava() throws Exception {
    assertThat("The expected project type is not the same",
               getProjectDestination(projectPath, ProjectType.JAVA),
               instanceOf(JavaProject.class));
  }

  @Test
  public void getProjectMatchMuleApplication() throws Exception {
    assertThat("The expected project type is not the same",
               getProjectDestination(projectPath, ProjectType.MULE_THREE_APPLICATION),
               instanceOf(MuleFourApplication.class));
  }

  @Test
  public void getProjectMatchMuleDomain() throws Exception {
    assertThat("The expected project type is not the same",
               getProjectDestination(projectPath, ProjectType.MULE_THREE_DOMAIN),
               instanceOf(MuleFourDomain.class));
  }

}
