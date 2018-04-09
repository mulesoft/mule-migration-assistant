/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.pom;

import com.mulesoft.tools.migration.pom.Dependency;
import com.mulesoft.tools.migration.pom.PomModel;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RemoveMuleDependenciesTest {

  private static final String POM_WITH_MULE_DEPENDENCIES = "/pommodel/muleDependencies/pom.xml";
  private String POM_WITHOUT_MULE_DEPENDENCIES = "/pommodel/simple-pom/pom.xml";
  private String POM_WITHOUT_DEPENDENCIES = "/pommodel/muleAppMavenPlugin/pom.xml";

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
    removeMuleDependencies.execute(model);
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
    removeMuleDependencies.execute(model);
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
    removeMuleDependencies.execute(model);
    assertThat("There should be no dependencies in the pom", model.getDependencies().isEmpty(), is(true));
  }
}
