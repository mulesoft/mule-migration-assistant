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
package com.mulesoft.tools.migration.library.munit.steps;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

import org.codehaus.plexus.util.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class MUnitPomContributionTest {

  private static final String SIMPLE_POM = "/pommodel/simple-pom/pom.xml";
  private PomModel model;

  private MUnitPomContribution munitPomContribution;

  @Rule
  public ReportVerification report = new ReportVerification();

  @Before
  public void setUp() throws Exception {
    munitPomContribution = new MUnitPomContribution();
  }

  @Test
  public void execute() throws Exception {
    Path pomPath = Paths.get(getClass().getResource(SIMPLE_POM).toURI());
    model = new PomModel.PomModelBuilder().withPom(pomPath).build();
    munitPomContribution.execute(model, report.getReport());

    assertThat("munit-maven-plugin should be present in pom", isPluginInModel(), is(true));
    assertThat("munit-runner dependency should be present in pom", isDependencyInModel("munit-runner"), is(true));
    assertThat("munit-tools dependency should be present in pom", isDependencyInModel("munit-tools"), is(true));
  }

  public boolean isDependencyInModel(String dependencyArtifactId) {
    return model.getDependencies().stream().anyMatch(dep -> StringUtils.equals(dep.getArtifactId(), dependencyArtifactId));
  }

  public boolean isPluginInModel() {
    return model.getPlugins().stream().anyMatch(plugin -> StringUtils.equals(plugin.getArtifactId(), "munit-maven-plugin"));
  }
}
