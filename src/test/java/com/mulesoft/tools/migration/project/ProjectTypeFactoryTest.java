/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.mulesoft.tools.migration.project.structure.ProjectType;
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
               is(ProjectType.BASIC));
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
    assertThat("The expected project type is not the same",
               projectTypeFactory.getProjectType(projectPath),
               is(ProjectType.MULE));
  }

  @Test
  public void getProjectTypeMuleThreeWithJavaFolder() throws Exception {
    createFolder("src/main/app");
    createFolder("src/main/java");
    assertThat("The expected project type is not the same",
               projectTypeFactory.getProjectType(projectPath),
               is(ProjectType.MULE));
  }

  @Test
  public void getProjectTypeMuleFour() throws Exception {
    createFolder("src/main/mule");
    assertThat("The expected project type is not the same",
               projectTypeFactory.getProjectType(projectPath),
               is(ProjectType.MULE_APPLICATION));
  }

  @Test
  public void getProjectTypeDomain() throws Exception {
    createFolder("src/main/domain");
    assertThat("The expected project type is not the same",
               projectTypeFactory.getProjectType(projectPath),
               is(ProjectType.MULE_DOMAIN));
  }

  public void createFolder(String path) {
    File app = projectPath.resolve(path).toFile();
    app.mkdirs();
  }
}
