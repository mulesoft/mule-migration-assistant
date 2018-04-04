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

import static org.apache.commons.lang3.StringUtils.EMPTY;

@RunWith(Enclosed.class)
public class PluginExecutionTestCase {

  public static class PluginExecutionBuilderTest {

    private PluginExecution.PluginExecutionBuilder builder;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
      builder = new PluginExecution.PluginExecutionBuilder();
    }

    @Test
    public void buildWithEmptyId() {
      expectedException.expect(IllegalArgumentException.class);
      expectedException.expectMessage("Id should not be blank");
      builder.withId(EMPTY).build();
    }

    @Test
    public void buildWithEmptyPhase() {
      expectedException.expect(IllegalArgumentException.class);
      expectedException.expectMessage("Phase should not be blank");
      builder.withPhase(EMPTY).build();
    }
  }
}
