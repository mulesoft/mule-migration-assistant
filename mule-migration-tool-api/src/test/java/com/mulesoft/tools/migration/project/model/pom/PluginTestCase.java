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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.*;

import static com.mulesoft.tools.migration.project.model.pom.PomModelTestCaseUtils.ARTIFACT_ID;
import static com.mulesoft.tools.migration.project.model.pom.PomModelTestCaseUtils.GROUP_ID;
import static com.mulesoft.tools.migration.project.model.pom.PomModelTestCaseUtils.VERSION;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(Enclosed.class)
public class PluginTestCase {

  public static class PluginBuilderTest {

    private Plugin.PluginBuilder builder;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
      builder = new Plugin.PluginBuilder();
      builder.withArtifactId(ARTIFACT_ID);
      builder.withGroupId(GROUP_ID);
      builder.withVersion(VERSION);
    }

    @Test
    public void buildWithNullArtifactId() {
      expectedException.expect(IllegalArgumentException.class);
      expectedException.expectMessage("Artifact id cannot be null nor empty");
      builder.withArtifactId(null).build();
    }

    @Test
    public void buildWithEmptyArtifactId() {
      expectedException.expect(IllegalArgumentException.class);
      expectedException.expectMessage("Artifact id cannot be null nor empty");
      builder.withArtifactId(EMPTY).build();
    }

    @Test
    public void buildWithNullGroupId() {
      expectedException.expect(IllegalArgumentException.class);
      expectedException.expectMessage("Group id cannot be null nor empty");
      builder.withGroupId(null).build();
    }

    @Test
    public void buildWithEmptyGroupId() {
      expectedException.expect(IllegalArgumentException.class);
      expectedException.expectMessage("Group id cannot be null nor empty");
      builder.withGroupId(EMPTY).build();
    }

    @Test
    public void buildWithNullVersion() {
      expectedException.expect(IllegalArgumentException.class);
      expectedException.expectMessage("Version cannot be null nor empty");
      builder.withVersion(null).build();
    }

    @Test
    public void buildWithEmptyVersion() {
      expectedException.expect(IllegalArgumentException.class);
      expectedException.expectMessage("Version cannot be null nor empty");
      builder.withVersion(EMPTY).build();
    }

    @Test
    public void areAllExecutionIdsUniqueFalse() {
      List<PluginExecution> pluginExecutions = new ArrayList<>();
      pluginExecutions.add(new PluginExecution.PluginExecutionBuilder().build());
      pluginExecutions.add(new PluginExecution.PluginExecutionBuilder().build());
      assertThat("Method should have returned false", builder.areAllExecutionIdsUnique(pluginExecutions), equalTo(false));
    }

    @Test
    public void areAllExecutionIdsUniqueTrue() {
      PluginExecution pluginExecution1 = new PluginExecution.PluginExecutionBuilder().withId("id1").build();
      PluginExecution pluginExecution2 = new PluginExecution.PluginExecutionBuilder().withId("id2").build();

      List<PluginExecution> pluginExecutions = new ArrayList<>();
      pluginExecutions.add(pluginExecution1);
      pluginExecutions.add(pluginExecution2);

      assertThat("Method should have returned true", builder.areAllExecutionIdsUnique(pluginExecutions), equalTo(true));
    }
  }
}
