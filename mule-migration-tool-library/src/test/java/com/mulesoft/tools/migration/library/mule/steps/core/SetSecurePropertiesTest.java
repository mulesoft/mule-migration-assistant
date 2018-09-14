/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.project.model.artifact.MuleArtifactJsonModel;
import com.mulesoft.tools.migration.project.model.artifact.MuleArtifactJsonModelUtils;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import static com.google.common.collect.Lists.newArrayList;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;

import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;

public class SetSecurePropertiesTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private Path projectBasePath;
  private SetSecureProperties setSecureProperties;
  private File muleArtifactJsonFile;
  private final String MIN_MULE_VERSION = "4.1.2";
  private File muleAppProperties;

  @Before
  public void setUp() throws IOException {
    projectBasePath = temporaryFolder.getRoot().toPath();
    File resources = new File(projectBasePath.toFile(), "src/main/resources");
    resources.mkdirs();
    muleAppProperties = new File(resources, "mule-app.properties");
    FileUtils.write(muleAppProperties, "secure.properties=lala, pepe", UTF_8);
    setSecureProperties = new SetSecureProperties();
    muleArtifactJsonFile = new File(projectBasePath.toFile(), "mule-artifact.json");
    muleArtifactJsonFile.createNewFile();
    FileUtils.write(muleArtifactJsonFile, "{ minMuleVersion: " + MIN_MULE_VERSION + " }", UTF_8);

  }

  @Test
  public void execute() throws IOException {
    setSecureProperties.execute(projectBasePath, mock(MigrationReport.class));
    MuleArtifactJsonModel model = MuleArtifactJsonModelUtils.buildMuleArtifactJson(muleArtifactJsonFile.toPath());

    assertThat("Secure properties were not created successfully", model.getSecureProperties(),
               equalTo(newArrayList("lala", "pepe")));

    Properties properties = new Properties();
    properties.load(new FileInputStream(muleAppProperties));
    assertThat("Secure properties should not exist", properties.containsKey("secure.properties"), is(false));

  }
}
