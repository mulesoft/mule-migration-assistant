/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.pom;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RemoveMuleAppMavenPluginTest {

  private static final String POM_WITH_MULE_APP_MAVEN_PLUGIN = "/pommodel/muleAppMavenPlugin/pom.xml";
  private static final String POM_WITH_MULE_APP_MAVEN_PLUGIN_IN_PROFILE = "/pommodel/muleAppMavenPluginInProfile/pom.xml";
  private static final String POM_WITHOUT_MULE_APP_MAVEN_PLUGIN = "/pommodel/simple-pom/pom.xml";
  private PomModel model;
  private RemoveMuleAppMavenPlugin removeMuleAppMavenPlugin;

  @Before
  public void setUp() {
    removeMuleAppMavenPlugin = new RemoveMuleAppMavenPlugin();
  }

  @Test
  public void executeWhenMuleAppMavenPluginIsPresent() throws IOException, XmlPullParserException, URISyntaxException {
    Path pomPath = Paths.get(getClass().getResource(POM_WITH_MULE_APP_MAVEN_PLUGIN).toURI());
    model = new PomModel.PomModelBuilder().withPom(pomPath).build();
    assertThat("mule-app-maven-plugin should be present in pom", isPluginInModel(), is(true));
    removeMuleAppMavenPlugin.execute(model, mock(MigrationReport.class));
    assertThat("mule-app-maven-plugin should not be present in pom", isPluginInModel(), is(false));
  }

  @Test
  public void executeWhenMuleAppMavenPluginIsPresentInProfile() throws IOException, XmlPullParserException, URISyntaxException {
    Path pomPath = Paths.get(getClass().getResource(POM_WITH_MULE_APP_MAVEN_PLUGIN_IN_PROFILE).toURI());
    model = new PomModel.PomModelBuilder().withPom(pomPath).build();
    assertThat("mule-app-maven-plugin should be present in pom", isPluginInModel(), is(true));
    removeMuleAppMavenPlugin.execute(model, mock(MigrationReport.class));
    assertThat("mule-app-maven-plugin should not be present in pom", isPluginInModel(), is(false));
  }

  @Test
  public void executeWhenMuleAppMavenPluginIsNotPresent() throws IOException, XmlPullParserException, URISyntaxException {
    Path pomPath = Paths.get(getClass().getResource(POM_WITHOUT_MULE_APP_MAVEN_PLUGIN).toURI());
    model = new PomModel.PomModelBuilder().withPom(pomPath).build();
    assertThat("mule-app-maven-plugin should not be present in pom", isPluginInModel(), is(false));
    removeMuleAppMavenPlugin.execute(model, mock(MigrationReport.class));
    assertThat("mule-app-maven-plugin should not be present in pom", isPluginInModel(), is(false));
  }

  public boolean isPluginInModel() {
    return model.getPlugins().stream().anyMatch(plugin -> StringUtils.equals(plugin.getArtifactId(), "mule-app-maven-plugin"))
        || model.getProfiles().stream().flatMap(profile -> profile.getBuild().getPlugins().stream())
            .anyMatch(plugin -> StringUtils.equals(plugin.getArtifactId(), "mule-app-maven-plugin"));
  }
}
