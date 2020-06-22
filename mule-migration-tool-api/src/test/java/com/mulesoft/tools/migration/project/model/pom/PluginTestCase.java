/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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
