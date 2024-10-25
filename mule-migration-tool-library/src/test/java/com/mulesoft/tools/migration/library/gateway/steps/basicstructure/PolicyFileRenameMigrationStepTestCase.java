/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.basicstructure;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.library.gateway.steps.policy.basicstructure.PolicyFileRenameMigrationStep;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.After;
import org.junit.Test;

public class PolicyFileRenameMigrationStepTestCase {

  private static final Path APPLICATION_MODEL_PATH = Paths.get("src/test/resources/mule/apps/gateway/file-system");

  private ApplicationModel appModel;

  @Test
  public void renameTest() throws Exception {
    MigrationReport migrationReport = mock(MigrationReport.class);
    ApplicationModel.ApplicationModelBuilder amb = new ApplicationModel.ApplicationModelBuilder();
    amb.withProjectType(ProjectType.MULE_THREE_POLICY);
    amb.withConfigurationFiles(Arrays.asList(APPLICATION_MODEL_PATH.resolve("src/main/mule/simple-test-policy.xml")));
    amb.withProjectBasePath(APPLICATION_MODEL_PATH);
    appModel = amb.build();
    PolicyFileRenameMigrationStep step = new PolicyFileRenameMigrationStep();
    step.setApplicationModel(appModel);
    step.execute(APPLICATION_MODEL_PATH, migrationReport);
    assertThat(APPLICATION_MODEL_PATH.resolve(appModel.getPomModel().get().getArtifactId() + ".yaml").toFile().exists(),
               is(true));
    assertThat(APPLICATION_MODEL_PATH.resolve("simple-test-policy.yaml").toFile().exists(), is(false));
    Path sourcesFilePath = APPLICATION_MODEL_PATH.resolve("src/main/mule");
    assertThat(sourcesFilePath.resolve("template.xml").toFile().exists(), is(true));
    assertThat(sourcesFilePath.resolve("simple-test-policy.xml").toFile().exists(), is(false));
    assertThat(appModel.getApplicationDocuments().size(), is(1));
    assertThat(appModel.getApplicationDocuments().get(Paths.get("src/main/mule/template.xml")), notNullValue());
    assertThat(appModel.getApplicationDocuments().get(Paths.get("src/main/mule/simple-test-policy.xml")), nullValue());
  }

  @After
  public void setOriginalNames() {
    APPLICATION_MODEL_PATH.resolve(appModel.getPomModel().get().getArtifactId() + ".yaml").toFile()
        .renameTo(APPLICATION_MODEL_PATH.resolve("simple-test-policy.yaml").toFile());
    Path sourcesFilePath = APPLICATION_MODEL_PATH.resolve("src/main/mule");
    sourcesFilePath.resolve("template.xml").toFile().renameTo(sourcesFilePath.resolve("simple-test-policy.xml").toFile());
  }
}
