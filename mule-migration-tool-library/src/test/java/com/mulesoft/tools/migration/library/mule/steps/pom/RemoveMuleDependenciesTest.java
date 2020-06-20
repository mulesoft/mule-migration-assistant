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
package com.mulesoft.tools.migration.library.mule.steps.pom;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;

public class RemoveMuleDependenciesTest {

  private static final String POM_WITH_MULE_DEPENDENCIES = "/pommodel/muleDependencies/pom.xml";
  private String POM_WITHOUT_MULE_DEPENDENCIES = "/pommodel/simple-pom/pom.xml";
  private String POM_WITHOUT_DEPENDENCIES = "/pommodel/muleAppMavenPlugin/pom.xml";

  @Rule
  public ReportVerification report = new ReportVerification();

  private PomModel model;
  private RemoveMuleDependencies removeMuleDependencies;
  private static final Predicate<Dependency> isMuleDependency =
      d -> d.getGroupId().startsWith("org.mule.") || d.getGroupId().startsWith("com.mulesoft.muleesb");

  @Before
  public void setUp() {
    removeMuleDependencies = new RemoveMuleDependencies();
  }

  @Test
  public void executeGeneralTest() throws IOException, XmlPullParserException, URISyntaxException {
    Path pomPath = Paths.get(getClass().getResource(POM_WITH_MULE_DEPENDENCIES).toURI());
    model = new PomModel.PomModelBuilder().withPom(pomPath).build();
    assertThat("There should be 6 mule dependencies in the pom",
               model.getDependencies().stream().filter(isMuleDependency).collect(toList()).size(), equalTo(6));
    assertThat("Number of dependencies in pom should be 10", model.getDependencies().size(), equalTo(10));
    removeMuleDependencies.execute(model, report.getReport());
    assertThat("Number of dependencies in pom should be 4", model.getDependencies().size(), equalTo(4));
    assertThat("There should be no mule dependencies in the pom", model.getDependencies().stream().anyMatch(isMuleDependency),
               is(false));
  }

  @Test
  public void executeWhenThereAreNoMuleDependencies() throws IOException, XmlPullParserException, URISyntaxException {
    Path pomPath = Paths.get(getClass().getResource(POM_WITHOUT_MULE_DEPENDENCIES).toURI());
    model = new PomModel.PomModelBuilder().withPom(pomPath).build();
    assertThat("There should be no mule dependencies in the pom", model.getDependencies().stream().anyMatch(isMuleDependency),
               is(false));
    assertThat("Number of dependencies in pom should be 4", model.getDependencies().size(), equalTo(4));
    removeMuleDependencies.execute(model, report.getReport());
    assertThat("Number of dependencies in pom should be 4", model.getDependencies().size(), equalTo(4));
    assertThat("There should be no mule dependencies in the pom", model.getDependencies().stream().anyMatch(isMuleDependency),
               is(false));
  }

  @Test
  public void executeWhenThereAreNoDependencies() throws IOException, XmlPullParserException, URISyntaxException {
    Path pomPath = Paths.get(getClass().getResource(POM_WITHOUT_DEPENDENCIES).toURI());
    model = new PomModel.PomModelBuilder().withPom(pomPath).build();
    assertThat("There should be no mule dependencies in the pom", model.getDependencies().stream().anyMatch(isMuleDependency),
               is(false));
    assertThat("There should be no dependencies in the pom", model.getDependencies().isEmpty(), is(true));
    removeMuleDependencies.execute(model, report.getReport());
    assertThat("There should be no dependencies in the pom", model.getDependencies().isEmpty(), is(true));
  }
}
