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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.mulesoft.tools.migration.helper.MavenTestHelper;
import com.mulesoft.tools.migration.helper.MuleConfigTestHelper;
import com.mulesoft.tools.migration.project.ProjectType;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Path;

/**
 * @author Mulesoft Inc.
 */
public class ProjectTypeFactoryTest {

  private static final String APP_NAME = "test-app";

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private ProjectTypeFactory projectTypeFactory = new ProjectTypeFactory();
  private Path projectPath;

  @Before
  public void setUp() throws Exception {
    projectPath = temporaryFolder.newFolder(APP_NAME).toPath();
  }

  @Test
  public void getProjectTypeBasic() throws Exception {
    assertThat("The expected project type is not the same",
               projectTypeFactory.getProjectType(projectPath),
               Matchers.is(ProjectType.BASIC));
  }

  @Test
  public void getProjectTypeJava() throws Exception {
    createFolder("src/main/java");
    assertThat("The expected project type is not the same",
               projectTypeFactory.getProjectType(projectPath),
               is(ProjectType.JAVA));
  }

  @Test
  public void getProjectTypeMuleThree() throws Exception {
    createFolder("src/main/app");
    FileUtils.write(new File(projectPath.resolve("src/main/app").toFile(), "mule.xml"), MuleConfigTestHelper.emptyMuleConfig(),
                    UTF_8);
    assertThat("The expected project type is not the same",
               projectTypeFactory.getProjectType(projectPath),
               is(ProjectType.MULE_THREE_APPLICATION));
  }

  @Test
  public void getProjectTypeMuleThreeMavenized() throws Exception {
    createFolder("src/main/app");
    FileUtils.write(new File(projectPath.toFile(), "pom.xml"), MavenTestHelper.emptyPom(), UTF_8);
    assertThat("The expected project type is not the same",
               projectTypeFactory.getProjectType(projectPath),
               is(ProjectType.MULE_THREE_MAVEN_APPLICATION));
  }

  @Test
  public void getProjectTypeMuleThreeWithJavaFolder() throws Exception {
    createFolder("src/main/app");
    createFolder("src/main/java");
    FileUtils.write(new File(projectPath.toFile(), "pom.xml"), MavenTestHelper.emptyPom(), UTF_8);
    assertThat("The expected project type is not the same",
               projectTypeFactory.getProjectType(projectPath),
               is(ProjectType.MULE_THREE_MAVEN_APPLICATION));
  }

  @Test
  public void getProjectTypeMuleFour() throws Exception {
    createFolder("src/main/mule");
    assertThat("The expected project type is not the same",
               projectTypeFactory.getProjectType(projectPath),
               is(ProjectType.MULE_FOUR_APPLICATION));
  }

  @Test
  public void getProjectTypeDomain() throws Exception {
    createFolder("src/main/domain");
    FileUtils.write(new File(projectPath.resolve("src/main/domain").toFile(), "domain.xml"),
                    MuleConfigTestHelper.emptyMuleDomainConfig(), UTF_8);
    assertThat("The expected project type is not the same",
               projectTypeFactory.getProjectType(projectPath),
               is(ProjectType.MULE_THREE_DOMAIN));
  }

  @Test
  public void getProjectTypeDomainMavenized() throws Exception {
    createFolder("src/main/domain");
    FileUtils.write(new File(projectPath.toFile(), "pom.xml"), MavenTestHelper.emptyPom(), UTF_8);
    assertThat("The expected project type is not the same",
               projectTypeFactory.getProjectType(projectPath),
               is(ProjectType.MULE_THREE_MAVEN_DOMAIN));
  }

  public void createFolder(String path) {
    File app = projectPath.resolve(path).toFile();
    app.mkdirs();
  }
}
