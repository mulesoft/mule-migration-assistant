/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
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
