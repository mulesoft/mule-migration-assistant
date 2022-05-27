/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.pom;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
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
  private static final String POM_NO_VERSION = "/pommodel/simple-pom/pom-with-parent-no-version.xml";

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

  @Test
  public void executeForMissingVersion() throws IOException, XmlPullParserException, URISyntaxException {
    Path pomPath = Paths.get(getClass().getResource(POM_NO_VERSION).toURI());
    model = new PomModel.PomModelBuilder().withPom(pomPath).build();
    assertThat("version must not be updated in pom", model.getVersion(), nullValue());
    updateProjectVersion.execute(model, report.getReport());
    assertThat("version must not be updated in pom", model.getVersion(), nullValue());
  }

}
