/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.pom;

import static com.google.common.collect.Sets.newHashSet;
import static com.mulesoft.tools.migration.project.model.pom.PomModelUtils.MULE_MAVEN_PLUGIN_ARTIFACT_ID;
import static com.mulesoft.tools.migration.project.model.pom.PomModelUtils.MULE_MAVEN_PLUGIN_VERSION;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.mulesoft.tools.migration.project.model.pom.Plugin;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class UpdateMuleMavenPluginTest {

  private UpdateMuleMavenPlugin updateMuleMavenPlugin;
  private Xpp3Dom from;
  private Xpp3Dom to;
  private Xpp3Dom child;
  private static final String CHILD_NAME = "childName";
  private Xpp3Dom configuration;
  private String deploymentConfigurationName = StringUtils.EMPTY;
  private UpdateMuleMavenPlugin updateMuleMavenPluginSpy;

  private String POM_WITH_MULE_MAVEN_PLUGIN = "/pommodel/muleMavenPlugin/pom.xml";
  private PomModel model;

  @Rule
  public ReportVerification report = new ReportVerification();

  @Before
  public void setUp() {
    updateMuleMavenPlugin = new UpdateMuleMavenPlugin();
    from = new Xpp3Dom("from");
    to = new Xpp3Dom("to");
    child = new Xpp3Dom(CHILD_NAME);
    configuration = new Xpp3Dom("configuration");
    updateMuleMavenPluginSpy = spy(updateMuleMavenPlugin);
  }

  @Test
  public void execute() throws IOException, XmlPullParserException, URISyntaxException {
    Path pomPath = Paths.get(getClass().getResource(POM_WITH_MULE_MAVEN_PLUGIN).toURI());
    model = new PomModel.PomModelBuilder().withPom(pomPath).build();
    updateMuleMavenPlugin.execute(model, report.getReport());

    Plugin muleMavenPlugin =
        model.getPlugins().stream().filter(p -> p.getArtifactId().equals(MULE_MAVEN_PLUGIN_ARTIFACT_ID)).findFirst().get();
    assertThat("Version is not the expected", muleMavenPlugin.getVersion(), equalTo(MULE_MAVEN_PLUGIN_VERSION));

    Xpp3Dom[] configurationChildren = muleMavenPlugin.getConfiguration().getChildren();
    assertThat("Number of children is not the expected", configurationChildren.length, equalTo(1));

    Xpp3Dom standaloneConfiguration = configurationChildren[0];
    assertThat("Configuration does not have the expected name", standaloneConfiguration.getName(),
               equalTo("standaloneDeployment"));

    assertThat("Number of children is not the expected", standaloneConfiguration.getChildren().length, equalTo(2));
    assertThat("Child is not the expected", standaloneConfiguration.getChildren()[0].getName(), equalTo("deploymentTimeout"));
    assertThat("Child is not the expected", standaloneConfiguration.getChildren()[1].getName(), equalTo("muleHome"));
  }

  @Test
  public void updateConfigurationWithNewDeploymentConfigurationVerifyIsChild() {
    deploymentConfigurationName = "cloudhubDeployment";
    assertThat("Deployment configuration should not be a child of the configuration",
               configuration.getChild(deploymentConfigurationName), nullValue());
    updateMuleMavenPlugin.updateConfigurationWithNewDeploymentConfiguration(deploymentConfigurationName, configuration);
    assertThat("Deployment configuration should be a child of the configuration",
               configuration.getChild(deploymentConfigurationName), not(nullValue()));
  }

  @Test
  public void updateConfigurationWithNewDeploymentConfigurationMoveProperties() {
    deploymentConfigurationName = "cloudhubDeployment";
    doNothing().when(updateMuleMavenPluginSpy).moveChildWithName(anyString(), any(Xpp3Dom.class), any(Xpp3Dom.class));
    String property1 = "property1";
    String property2 = "property2";
    String property3 = "property3";
    String property4 = "property4";

    Set<String> deploymentParameters = newHashSet(property1, property2, property3, property4);
    doReturn(deploymentParameters).when(updateMuleMavenPluginSpy).getDeploymentParameters();

    updateMuleMavenPluginSpy.updateConfigurationWithNewDeploymentConfiguration(deploymentConfigurationName, configuration);

    verify(updateMuleMavenPluginSpy).getDeploymentParameters();
    verify(updateMuleMavenPluginSpy, times(deploymentParameters.size())).moveChildWithName(anyString(), any(Xpp3Dom.class),
                                                                                           any(Xpp3Dom.class));
  }

  @Test
  public void moveChildWithName() {
    from.addChild(child);

    assertThat("From node should have one child", from.getChildCount(), equalTo(1));
    assertThat("To node should have no children", to.getChildCount(), equalTo(0));

    updateMuleMavenPlugin.moveChildWithName(CHILD_NAME, from, to);

    assertThat("From node should have no children", from.getChildCount(), equalTo(0));
    assertThat("To node should have one child", to.getChildCount(), equalTo(1));
    assertThat("Child is not the expected", to.getChild(0).equals(child), is(true));
  }

  @Test
  public void moveChildWithNameNoExistent() {
    assertThat("From node should have no children", from.getChildCount(), equalTo(0));
    assertThat("To node should have no children", to.getChildCount(), equalTo(0));

    updateMuleMavenPlugin.moveChildWithName(CHILD_NAME, from, to);

    assertThat("From node should have no children", from.getChildCount(), equalTo(0));
    assertThat("To node should have no children", to.getChildCount(), equalTo(0));
  }

  @Test
  public void moveChildWithNameAlreadyInDestionationNode() {
    to.addChild(child);

    assertThat("From node should have no children", from.getChildCount(), equalTo(0));
    assertThat("To node should have one child", to.getChildCount(), equalTo(1));

    updateMuleMavenPlugin.moveChildWithName(CHILD_NAME, from, to);

    assertThat("From node should have no children", from.getChildCount(), equalTo(0));
    assertThat("To node should have one child", to.getChildCount(), equalTo(1));
    assertThat("Child is not the expected", to.getChild(0).equals(child), is(true));
  }

  @Test
  public void moveChildWithNameInBoth() {
    from.addChild(child);
    Xpp3Dom otherChildWithSameName = new Xpp3Dom(CHILD_NAME);
    to.addChild(otherChildWithSameName);

    assertThat("From node should have one child", from.getChildCount(), equalTo(1));
    assertThat("From node should have at least one child with this name", from.getChild(CHILD_NAME) != null, is(true));
    assertThat("To node should have one children", to.getChildCount(), equalTo(1));
    assertThat("To node should have at least one child with this name", to.getChild(CHILD_NAME) != null, is(true));


    updateMuleMavenPlugin.moveChildWithName(CHILD_NAME, from, to);

    assertThat("From node should have no children", from.getChildCount(), equalTo(0));
    assertThat("From node should have no children with this name", from.getChild(CHILD_NAME) == null, is(true));

    assertThat("To node should have one child", to.getChildCount(), equalTo(2));
    assertThat("To node should have at least one child with this name", to.getChild(CHILD_NAME) != null, is(true));

    assertThat("Child is not the expected", to.getChild(0).equals(child), is(true));
    assertThat("Child is not the expected", to.getChild(1).equals(otherChildWithSameName), is(true));
  }

  @Test
  public void moveChildWithNameNoChildrenInBoth() {
    assertThat("From node should have no children", from.getChildCount(), equalTo(0));
    assertThat("To node should have no children", to.getChildCount(), equalTo(0));

    updateMuleMavenPlugin.moveChildWithName(CHILD_NAME, from, to);

    assertThat("From node should have no children", from.getChildCount(), equalTo(0));
    assertThat("To node should have no children", to.getChildCount(), equalTo(0));
  }

}
