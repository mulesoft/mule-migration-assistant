/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.tasks.throttling;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.COM_MULESOFT_ANYPOINT_GROUP_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.HTTP_POLICY_TRANSFORM_EXTENSION_VERSION;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_HTTP_POLICY_TRANSFORM_EXTENSION_ARTIFACT_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_PLUGIN_CLASSIFIER;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_THROTTLING_EXTENSION_ARTIFACT_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.THROTTLING_EXTENSION_SLA_VERSION;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.THROTTLING_EXTENSION_VERSION;
import static com.mulesoft.tools.migration.library.gateway.tasks.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.library.gateway.tasks.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.gateway.tasks.ThrottlingMigrationTask;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.Dependency;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.nio.file.Path;
import java.nio.file.Paths;
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
public class ThrottlingMigrationTaskTestCase {

  private static final Path THROTTLING_EXAMPLES_PATH = Paths.get("mule/apps/gateway/throttling");
  private static final Path APPLICATION_MODEL_PATH =
      Paths.get("src/test/resources/mule/apps/gateway/throttling/expected");

  private final Path configPath;
  private final Path targetPath;
  private final Path pomPath;
  private final MigrationReport reportMock;
  private ApplicationModel appModel;
  private Document doc;

  private List<MigrationStep> steps;

  @Parameterized.Parameters(name = "{0}: {0}, {1}, {2}")
  public static Collection<Object[]> params() {
    return asList(new Object[][] {
        //        {"rate-limit3.xml", "rate-limit4.xml", "rate-limit-pom.xml"},
        //        {"throttling3.xml", "spike-control4.xml", "spike-control-pom.xml"},
        {"rate-limit-sla3.xml", "rate-limit-sla4.xml", "rate-limit-sla-pom.xml"},
        {"throttling-sla3.xml", "throttling-sla4.xml", "rate-limit-sla-pom.xml"},
    });
  }

  public ThrottlingMigrationTaskTestCase(final String original, final String target, final String pom) {
    configPath = THROTTLING_EXAMPLES_PATH.resolve("original/" + original);
    targetPath = THROTTLING_EXAMPLES_PATH.resolve("expected/" + target);
    pomPath = APPLICATION_MODEL_PATH.resolve(pom);
    reportMock = mock(MigrationReport.class);
  }

  @Before
  public void setUp() throws Exception {
    ApplicationModel.ApplicationModelBuilder amb = new ApplicationModel.ApplicationModelBuilder();
    amb.withProjectType(ProjectType.MULE_THREE_POLICY);
    amb.withProjectBasePath(APPLICATION_MODEL_PATH);
    amb.withPom(pomPath);
    appModel = amb.build();

    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    ThrottlingMigrationTask throttlingMigrationTask = new ThrottlingMigrationTask();
    throttlingMigrationTask.setApplicationModel(appModel);
    steps = throttlingMigrationTask.getSteps();
  }

  private void migrate(MigrationStep migrationStep) {
    if (migrationStep instanceof AbstractApplicationModelMigrationStep) {
      getElementsFromDocument(doc, ((AbstractApplicationModelMigrationStep) migrationStep).getAppliedTo().getExpression())
          .forEach(node -> migrationStep.execute(node, reportMock));
    } else {
      migrationStep.execute(appModel.getPomModel().get(), reportMock);
    }
  }

  private void assertPomModel() {
    PomModel pomModel = appModel.getPomModel().get();
    assertThat(pomModel.getDependencies().size(), is(2));
    Dependency httpTransformDependency = pomModel.getDependencies().get(0);
    assertThat(httpTransformDependency.getGroupId(), is(COM_MULESOFT_ANYPOINT_GROUP_ID));
    assertThat(httpTransformDependency.getArtifactId(), is(MULE_HTTP_POLICY_TRANSFORM_EXTENSION_ARTIFACT_ID));
    assertThat(httpTransformDependency.getVersion(), is(HTTP_POLICY_TRANSFORM_EXTENSION_VERSION));
    assertThat(httpTransformDependency.getClassifier(), is(MULE_PLUGIN_CLASSIFIER));
    Dependency policyTransformExtensionDependency = pomModel.getDependencies().get(1);
    assertThat(policyTransformExtensionDependency.getGroupId(), is(COM_MULESOFT_ANYPOINT_GROUP_ID));
    assertThat(policyTransformExtensionDependency.getArtifactId(), is(MULE_THROTTLING_EXTENSION_ARTIFACT_ID));
    if (pomModel.getArtifactId().equals("rate-limiting-sla-based")) {
      assertThat(policyTransformExtensionDependency.getVersion(), is(THROTTLING_EXTENSION_SLA_VERSION));
    } else {
      assertThat(policyTransformExtensionDependency.getVersion(), is(THROTTLING_EXTENSION_VERSION));
    }
    assertThat(policyTransformExtensionDependency.getClassifier(), is(MULE_PLUGIN_CLASSIFIER));
  }

  @Test
  public void execute() throws Exception {
    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    steps.forEach(step -> migrate(step));

    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
    assertPomModel();
  }

}
