/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
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
