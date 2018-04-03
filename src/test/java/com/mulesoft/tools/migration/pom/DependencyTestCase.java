/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.pom;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static com.mulesoft.tools.migration.pom.PomModelTestCaseUtils.ARTIFACT_ID;
import static com.mulesoft.tools.migration.pom.PomModelTestCaseUtils.GROUP_ID;
import static com.mulesoft.tools.migration.pom.PomModelTestCaseUtils.VERSION;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@RunWith(Enclosed.class)
public class DependencyTestCase {

  public static class DependencyBuilderTest {

    private Dependency.DependencyBuilder builder;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
      builder = new Dependency.DependencyBuilder();
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
    public void buildWithEmptyClassifier() {
      expectedException.expect(IllegalArgumentException.class);
      expectedException.expectMessage("Classifier should not be blank");
      builder.withClassifier(EMPTY).build();
    }

    @Test
    public void buildWithEmptyType() {
      expectedException.expect(IllegalArgumentException.class);
      expectedException.expectMessage("Type should not be blank");
      builder.withType(EMPTY).build();
    }

    @Test
    public void buildWithEmptyScope() {
      expectedException.expect(IllegalArgumentException.class);
      expectedException.expectMessage("Scope should not be blank");
      builder.withScope(EMPTY).build();
    }
  }
}
