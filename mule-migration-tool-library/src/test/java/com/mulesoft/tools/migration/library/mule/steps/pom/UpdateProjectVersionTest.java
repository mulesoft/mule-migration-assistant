/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.pom;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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

public class UpdateProjectVersionTest {

  private static final String POM = "/pommodel/simple-pom/pom.xml";
  private static final String POM_SNAPSHOT = "/pommodel/simple-pom/snapshot-pom.xml";

  @Rule
  public ReportVerification report = new ReportVerification();

  private PomModel model;
  private UpdateProjectVersion updateProjectVersion;

  @Before
  public void setUp() {
    updateProjectVersion = new UpdateProjectVersion();
  }

  @Test
  public void execute() throws IOException, XmlPullParserException, URISyntaxException {
    Path pomPath = Paths.get(getClass().getResource(POM).toURI());
    model = new PomModel.PomModelBuilder().withPom(pomPath).build();
    assertThat("version must not be updated in pom", model.getVersion(), is("1.0.0"));
    updateProjectVersion.execute(model, report.getReport());
    assertThat("version must be updated in pom", model.getVersion(), is("1.0.0-M4"));
  }

  @Test
  public void executeForSnapshot() throws IOException, XmlPullParserException, URISyntaxException {
    Path pomPath = Paths.get(getClass().getResource(POM_SNAPSHOT).toURI());
    model = new PomModel.PomModelBuilder().withPom(pomPath).build();
    assertThat("version must not be updated in pom", model.getVersion(), is("1.0.0-SNAPSHOT"));
    updateProjectVersion.execute(model, report.getReport());
    assertThat("version must be updated in pom", model.getVersion(), is("1.0.0-M4-SNAPSHOT"));
  }

}
