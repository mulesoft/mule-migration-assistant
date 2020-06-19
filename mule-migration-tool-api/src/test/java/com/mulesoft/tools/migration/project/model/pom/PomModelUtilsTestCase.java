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
package com.mulesoft.tools.migration.project.model.pom;

import static com.mulesoft.tools.migration.project.ProjectType.MULE_FOUR_APPLICATION;
import static com.mulesoft.tools.migration.project.model.pom.PomModelTestCaseUtils.ARTIFACT_ID;
import static com.mulesoft.tools.migration.project.model.pom.PomModelTestCaseUtils.GROUP_ID;
import static com.mulesoft.tools.migration.project.model.pom.PomModelTestCaseUtils.VERSION;
import static com.mulesoft.tools.migration.project.model.pom.PomModelUtils.DEFAULT_MODEL_VERSION;
import static com.mulesoft.tools.migration.project.model.pom.PomModelUtils.MULE_APPLICATION_4_PACKAGING_TYPE;
import static com.mulesoft.tools.migration.project.model.pom.PomModelUtils.MULE_MAVEN_PLUGIN_ARTIFACT_ID;
import static com.mulesoft.tools.migration.project.model.pom.PomModelUtils.MULE_MAVEN_PLUGIN_GROUP_ID;
import static com.mulesoft.tools.migration.project.model.pom.PomModelUtils.MULE_MAVEN_PLUGIN_VERSION;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import java.util.List;

public class PomModelUtilsTestCase {

  private static final PomModel MINIMAL_MULE_4_POM_MODEL =
      PomModelUtils.buildMinimalMule4ApplicationPom(GROUP_ID, ARTIFACT_ID, VERSION, MULE_FOUR_APPLICATION.getPackaging());

  @Test
  public void getGroupId() {
    assertThat("Group id is not the expected", MINIMAL_MULE_4_POM_MODEL.getGroupId(), equalTo(GROUP_ID));
  }

  @Test
  public void getArtifacId() {
    assertThat("Artifact id is not the expected", MINIMAL_MULE_4_POM_MODEL.getArtifactId(), equalTo(ARTIFACT_ID));
  }

  @Test
  public void getVersion() {
    assertThat("Version is not the expected", MINIMAL_MULE_4_POM_MODEL.getVersion(), equalTo(VERSION));
  }

  @Test
  public void getPackaging() {
    assertThat("Packaging is not the expected", MINIMAL_MULE_4_POM_MODEL.getPackaging(),
               equalTo(MULE_APPLICATION_4_PACKAGING_TYPE));
  }

  @Test
  public void getModelVersion() {
    assertThat("Model version is not the expected", MINIMAL_MULE_4_POM_MODEL.getModelVersion(), equalTo(DEFAULT_MODEL_VERSION));
  }

  @Test
  public void getPlugins() {
    List<Plugin> plugins = MINIMAL_MULE_4_POM_MODEL.getPlugins();
    assertThat("Number of plugins is not the expected", plugins.size(), equalTo(1));
    assertThat("Plugin artifact id is not the expected", plugins.get(0).getArtifactId(), equalTo(MULE_MAVEN_PLUGIN_ARTIFACT_ID));
    assertThat("Plugin group id is not the expected", plugins.get(0).getGroupId(), equalTo(MULE_MAVEN_PLUGIN_GROUP_ID));
    assertThat("Plugin version is not the expected", plugins.get(0).getVersion(), equalTo(MULE_MAVEN_PLUGIN_VERSION));
  }
}
