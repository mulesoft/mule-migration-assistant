/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.pom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.Parent;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Rule;
import org.junit.Test;

public class UpdateProjectParentTest {

  private static final String POM = "/pommodel/simple-pom/pom.xml";
  private static final String PARENT_POM = "/pommodel/simple-pom/parent-pom.xml";

  @Rule
  public ReportVerification report = new ReportVerification();


  @Test
  public void executeWithoutProjectParentGavAndWithoutParent() throws IOException, XmlPullParserException, URISyntaxException {
    UpdateProjectParent updateProjectParent = new UpdateProjectParent();
    final ApplicationModel applicationModelMock = mock(ApplicationModel.class);
    when(applicationModelMock.getProjectPomParent()).thenReturn(Optional.empty());
    updateProjectParent.setApplicationModel(applicationModelMock);

    Path pomPath = Paths.get(getClass().getResource(POM).toURI());
    PomModel model = new PomModel.PomModelBuilder().withPom(pomPath).build();

    try {
      updateProjectParent.execute(model, report.getReport());
    } catch (RuntimeException e) {
      fail("no exception have to be thrown");
    }
  }

  @Test
  public void executeWithoutProjectParentGavAndWithParent() throws URISyntaxException, IOException, XmlPullParserException {
    UpdateProjectParent updateProjectParent = new UpdateProjectParent();
    final ApplicationModel applicationModelMock = mock(ApplicationModel.class);
    when(applicationModelMock.getProjectPomParent()).thenReturn(Optional.empty());
    updateProjectParent.setApplicationModel(applicationModelMock);

    Path pomPath = Paths.get(getClass().getResource(PARENT_POM).toURI());
    PomModel model = new PomModel.PomModelBuilder().withPom(pomPath).build();
    try {
      updateProjectParent.execute(model, report.getReport());
    } catch (RuntimeException e) {
      fail("no exception have to be thrown");
    }
  }

  @Test
  public void executeWithProjectParentGavAndWithoutParent() throws URISyntaxException, IOException, XmlPullParserException {
    UpdateProjectParent updateProjectParent = new UpdateProjectParent();
    final ApplicationModel applicationModelMock = mock(ApplicationModel.class);
    Parent parent = new Parent.ParentBuilder().withGroupId("com.mule").withArtifactId("rest-parent").withVersion("2.0.0").build();
    when(applicationModelMock.getProjectPomParent()).thenReturn(Optional.of(parent));
    updateProjectParent.setApplicationModel(applicationModelMock);

    Path pomPath = Paths.get(getClass().getResource(POM).toURI());
    PomModel model = new PomModel.PomModelBuilder().withPom(pomPath).build();
    try {
      updateProjectParent.execute(model, report.getReport());
    } catch (RuntimeException e) {
      fail("no exception have to be thrown");
    }
  }

  @Test
  public void executeWithGavAndParent() throws URISyntaxException, IOException, XmlPullParserException {
    UpdateProjectParent updateProjectParent = new UpdateProjectParent();
    final ApplicationModel applicationModelMock = mock(ApplicationModel.class);
    Parent parent = new Parent.ParentBuilder().withGroupId("com.mule").withArtifactId("rest-parent").withVersion("2.0.0").build();
    when(applicationModelMock.getProjectPomParent()).thenReturn(Optional.of(parent));
    updateProjectParent.setApplicationModel(applicationModelMock);

    Path pomPath = Paths.get(getClass().getResource(PARENT_POM).toURI());
    PomModel model = new PomModel.PomModelBuilder().withPom(pomPath).build();
    parent = model.getParent().get();
    assertEquals(parent.getGroupId(), "com.mule.parent");
    assertEquals(parent.getArtifactId(), "parent-rest");
    assertEquals(parent.getVersion(), "1.0.0");
    try {
      updateProjectParent.execute(model, report.getReport());
      Parent afterParent = model.getParent().get();
      assertEquals(afterParent.getGroupId(), "com.mule");
      assertEquals(afterParent.getArtifactId(), "rest-parent");
      assertEquals(afterParent.getVersion(), "2.0.0");
    } catch (RuntimeException e) {
      fail("no exception have to be thrown");
    }
  }
}
