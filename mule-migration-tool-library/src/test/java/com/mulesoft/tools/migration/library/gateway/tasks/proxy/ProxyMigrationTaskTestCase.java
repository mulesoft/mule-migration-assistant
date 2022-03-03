/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.tasks.proxy;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.COM_MULESOFT_ANYPOINT_GROUP_ID;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_PLUGIN_CLASSIFIER;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.MULE_WSDL_FUNCTIONS_EXTENSION_ARTIFACT_ID;
import static com.mulesoft.tools.migration.library.gateway.tasks.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.library.gateway.tasks.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.gateway.tasks.ProxyMigrationTask;
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
public class ProxyMigrationTaskTestCase {

  private static final Path PROXY_EXAMPLES_PATH = Paths.get("mule/apps/gateway/proxy");
  private static final Path APPLICATION_MODEL_PATH =
      Paths.get("src/test/resources/mule/apps/gateway/proxy/expected");
  private static final Path POM_PATH = APPLICATION_MODEL_PATH.resolve("pom.xml");

  protected static final String MULE_HTTP_PROXY_EXTENSION_ARTIFACT_ID = "mule-http-proxy-extension";

  private final Path configPath;
  private final Path targetPath;
  private final MigrationReport reportMock;
  private ApplicationModel appModel;
  private Document doc;

  private List<MigrationStep> steps;

  @Parameterized.Parameters(name = "{0}: {0}, {1}")
  public static Collection<Object[]> params() {
    return Arrays.asList(new Object[][] {
        {"proxy-mule3.xml", "proxy-mule4.xml"},
        {"custom-processor-mule3.xml", "custom-processor-mule4.xml"},
        {"wsdl-mule3.xml", "wsdl-mule4.xml"},
    });
  }

  public ProxyMigrationTaskTestCase(final String original, final String target) {
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
    ProxyMigrationTask task = new ProxyMigrationTask();
    task.setApplicationModel(appModel);
    steps = task.getSteps().stream()
        .filter(step -> step instanceof AbstractApplicationModelMigrationStep)
        .collect(toList());
  }

  private void migrate(AbstractApplicationModelMigrationStep migrationStep) {
    getElementsFromDocument(doc, migrationStep.getAppliedTo().getExpression())
        .forEach(node -> migrationStep.execute(node, reportMock));
  }

  private void assertCustomProcessorDependency(PomModel pomModel) {
    Dependency customProcessorDependency = pomModel.getDependencies().get(2);
    assertThat(customProcessorDependency.getGroupId(), is(COM_MULESOFT_ANYPOINT_GROUP_ID));
    assertThat(customProcessorDependency.getArtifactId(), is(MULE_HTTP_PROXY_EXTENSION_ARTIFACT_ID));
    assertThat(customProcessorDependency.getVersion(), is(notNullValue()));
    assertThat(customProcessorDependency.getClassifier(), is(MULE_PLUGIN_CLASSIFIER));
  }

  private void assertWsdlDependency(PomModel pomModel, int position) {
    Dependency wsdlExtensionDependency = pomModel.getDependencies().get(position);
    assertThat(wsdlExtensionDependency.getGroupId(), is(COM_MULESOFT_ANYPOINT_GROUP_ID));
    assertThat(wsdlExtensionDependency.getArtifactId(), is(MULE_WSDL_FUNCTIONS_EXTENSION_ARTIFACT_ID));
    assertThat(wsdlExtensionDependency.getVersion(), is(notNullValue()));
    assertThat(wsdlExtensionDependency.getClassifier(), is(MULE_PLUGIN_CLASSIFIER));
  }

  @Test
  public void execute() throws Exception {
    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    steps.forEach(step -> migrate((AbstractApplicationModelMigrationStep) step));

    String xmlString = outputter.outputString(doc);
    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());

    PomModel pm = appModel.getPomModel().get();
    String filename = configPath.toFile().getName();
    if (filename.equals("wsdl-mule3.xml")) {
      assertThat(pm.getDependencies().size(), is(3));
      assertWsdlDependency(pm, 2);
    } else if (filename.equals("custom-processor-mule3.xml")) {
      assertThat(pm.getDependencies().size(), is(3));
      assertCustomProcessorDependency(pm);
    } else {
      assertThat(pm.getDependencies().size(), is(4));
      assertCustomProcessorDependency(pm);
      assertWsdlDependency(pm, 3);
    }
  }
}
