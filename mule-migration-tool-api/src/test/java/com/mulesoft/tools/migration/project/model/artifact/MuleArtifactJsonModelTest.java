/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project.model.artifact;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;


public class MuleArtifactJsonModelTest {

  private static final String MULE_ARTIFACT_FILE = "mule-artifact.json";
  private static final String MULE_VERSION = "4.1.1";
  private MuleArtifactJsonModel muleArtifactJsonModel;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private MuleArtifactJsonModel.MuleApplicationJsonModelBuilder builder;

  @Before
  public void setUp() {
    builder = new MuleArtifactJsonModel.MuleApplicationJsonModelBuilder();
  }

  @Test(expected = IllegalArgumentException.class)
  public void buildNullPath() throws IOException {
    muleArtifactJsonModel = builder.withMuleArtifactJson(null).build();
  }

  @Test
  public void buildWithNonExistentMuleArtifact() throws IOException {
    muleArtifactJsonModel = builder.withMuleArtifactJson(temporaryFolder.getRoot().toPath().resolve(MULE_ARTIFACT_FILE))
        .withMuleVersion(MULE_VERSION)
        .build();
    assertThat("Name in mule-artifact.json is not the expected", muleArtifactJsonModel.toString(),
               containsString("\"name\": \"" + temporaryFolder.getRoot().getName() + "\""));
  }
}
