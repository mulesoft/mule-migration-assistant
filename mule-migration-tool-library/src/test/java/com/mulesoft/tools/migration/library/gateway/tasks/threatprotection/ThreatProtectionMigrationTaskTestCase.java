/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.tasks.threatprotection;

import static com.mulesoft.tools.migration.library.gateway.tasks.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.library.gateway.tasks.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.gateway.tasks.ThreatProtectionMigrationTask;
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
public class ThreatProtectionMigrationTaskTestCase {

  private static final Path THREAT_PROTECTION_EXAMPLES_PATH = Paths.get("mule/apps/gateway/threat-protection");
  private static final Path APPLICATION_MODEL_PATH =
      Paths.get("src/test/resources/mule/apps/gateway/threat-protection/expected");

  private static final String COM_MULESOFT_ANYPOINT_GROUP_ID = "com.mulesoft.anypoint";
  private static final String MULE_XML_THREAT_PROTECTION_EXTENSION_ARTIFACT_ID = "mule-xml-threat-protection-extension";
  private static final String MULE_JSON_THREAT_PROTECTION_EXTENSION_ARTIFACT_ID = "mule-json-threat-protection-extension";
  private static final String XML_JSON_THREAT_PROTECTION_EXTENSION_VERSION = "1.1.0";
  private static final String MULE_PLUGIN_CLASSIFIER = "mule-plugin";

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
        {"xml-threat-protection3.xml", "xml-threat-protection4.xml", "xml-pom.xml"},
        {"json-threat-protection3.xml", "json-threat-protection4.xml", "json-pom.xml"},
    });
  }

  public ThreatProtectionMigrationTaskTestCase(final String original, final String target, final String pom) {
    configPath = THREAT_PROTECTION_EXAMPLES_PATH.resolve("original/" + original);
    targetPath = THREAT_PROTECTION_EXAMPLES_PATH.resolve("expected/" + target);
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
    ThreatProtectionMigrationTask task = new ThreatProtectionMigrationTask();
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

  private void assertPomModel(boolean isXmlThreatProtection) {
    PomModel pomModel = appModel.getPomModel().get();
    Dependency threatProtectionDependency;
    if (isXmlThreatProtection) {
      assertThat(pomModel.getDependencies().size(), is(3));
      threatProtectionDependency = pomModel.getDependencies().get(2);
      assertThat(threatProtectionDependency.getArtifactId(), is(MULE_XML_THREAT_PROTECTION_EXTENSION_ARTIFACT_ID));
    } else {
      assertThat(pomModel.getDependencies().size(), is(2));
      threatProtectionDependency = pomModel.getDependencies().get(1);
      assertThat(threatProtectionDependency.getArtifactId(), is(MULE_JSON_THREAT_PROTECTION_EXTENSION_ARTIFACT_ID));
    }
    assertThat(threatProtectionDependency.getGroupId(), is(COM_MULESOFT_ANYPOINT_GROUP_ID));
    assertThat(threatProtectionDependency.getVersion(), is(XML_JSON_THREAT_PROTECTION_EXTENSION_VERSION));
    assertThat(threatProtectionDependency.getClassifier(), is(MULE_PLUGIN_CLASSIFIER));
  }

  @Test
  public void execute() throws Exception {
    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    steps.forEach(step -> migrate(step));

    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());

    assertPomModel(configPath.toFile().getName().equals("xml-threat-protection3.xml"));
  }

}
