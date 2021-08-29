/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.tasks.raml;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.COM_MULESOFT_ANYPOINT_GROUP_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_PLUGIN_CLASSIFIER;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_REST_VALIDATOR_EXTENSION;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_REST_VALIDATOR_EXTENSION_VERSION;
import static com.mulesoft.tools.migration.library.gateway.tasks.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.library.gateway.tasks.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.gateway.tasks.RamlProxyMigrationTask;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class RamlProxyMigrationTaskTestCase {

  private static final Path PROXY_EXAMPLES_PATH = Paths.get("mule/apps/gateway/proxy/raml");
  private static final Path APPLICATION_MODEL_PATH =
      Paths.get("src/test/resources/mule/apps/gateway/proxy/raml/expected");
  private static final Path POM_PATH = APPLICATION_MODEL_PATH.resolve("pom.xml");

  private final Path configPath;
  private final Path targetPath;
  private final MigrationReport reportMock;
  private ApplicationModel appModel;
  private Document doc;

  private List<MigrationStep> steps;

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Collection<Object[]> params() {
    return Arrays.asList(new Object[][] {
        {"raml-proxy.xml", "rest-proxy.xml"},
        {"raml-proxy-hardcoded.xml", "rest-proxy-hardcoded.xml"},
        {"raml-proxy-unbound.xml", "rest-proxy-unbound.xml"},
    });
  }

  public RamlProxyMigrationTaskTestCase(final String original, final String target) {
    configPath = PROXY_EXAMPLES_PATH.resolve("original/" + original);
    targetPath = PROXY_EXAMPLES_PATH.resolve("expected/" + target);
    reportMock = mock(MigrationReport.class);
  }

  @Before
  public void setUp() throws Exception {
    ApplicationModel.ApplicationModelBuilder amb = new ApplicationModel.ApplicationModelBuilder();
    amb.withProjectType(ProjectType.MULE_THREE_POLICY);
    amb.withProjectBasePath(APPLICATION_MODEL_PATH);
    amb.withPom(POM_PATH);
    appModel = amb.build();

    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    RamlProxyMigrationTask task = new RamlProxyMigrationTask();
    task.setApplicationModel(appModel);
    steps = task.getSteps();
  }

  private void migrate(MigrationStep migrationStep) {
    if (migrationStep instanceof AbstractApplicationModelMigrationStep) {
      getElementsFromDocument(doc, ((AbstractApplicationModelMigrationStep) migrationStep).getAppliedTo().getExpression())
          .forEach(node -> migrationStep.execute(node, reportMock));
    } else {
      migrationStep.execute(appModel.getPomModel().get(), reportMock);
    }
  }

  private void assertRestValidatorDependency(PomModel pomModel) {
    Dependency customProcessorDependency = pomModel.getDependencies().get(0);
    assertThat(customProcessorDependency.getGroupId(), is(COM_MULESOFT_ANYPOINT_GROUP_ID));
    assertThat(customProcessorDependency.getArtifactId(), is(MULE_REST_VALIDATOR_EXTENSION));
    assertThat(customProcessorDependency.getVersion(), is(MULE_REST_VALIDATOR_EXTENSION_VERSION));
    assertThat(customProcessorDependency.getClassifier(), is(MULE_PLUGIN_CLASSIFIER));
  }

  @Test
  public void execute() throws Exception {
    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    steps.forEach(step -> migrate(step));

    String xmlString = outputter.outputString(doc);
    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());

    PomModel pm = appModel.getPomModel().get();
    assertThat(pm.getDependencies().size(), is(1));
    assertRestValidatorDependency(pm);
  }

}
