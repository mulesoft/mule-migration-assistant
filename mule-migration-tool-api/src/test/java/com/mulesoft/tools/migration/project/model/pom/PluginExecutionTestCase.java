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

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

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

  public static class PluginExecutionTest {

    private PluginExecution pluginExecution;

    @Before
    public void setUp() {
      pluginExecution = new PluginExecution.PluginExecutionBuilder()
          .withId("testId")
          .withPhase("testPhase")
          .withGoals(Arrays.asList("testGoal"))
          .build();
    }

    @Test
    public void setConfiguration() {
      Xpp3Dom simpleConfigurationElement = new Xpp3Dom("configuration");
      Xpp3Dom childElement = new Xpp3Dom("childElementKey");
      childElement.setValue("childElementValue");
      simpleConfigurationElement.addChild(childElement);
      pluginExecution.setConfiguration(simpleConfigurationElement);

      Xpp3Dom pluginConfiguration = pluginExecution.getConfiguration();
      assertNotNull(pluginConfiguration);
      Xpp3Dom testChildElement = pluginConfiguration.getChild("childElementKey");
      assertNotNull(testChildElement);
      assertThat(testChildElement.getValue(), is("childElementValue"));
    }
  }
}
