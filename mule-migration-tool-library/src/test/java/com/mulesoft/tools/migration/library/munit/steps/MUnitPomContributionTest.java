/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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
