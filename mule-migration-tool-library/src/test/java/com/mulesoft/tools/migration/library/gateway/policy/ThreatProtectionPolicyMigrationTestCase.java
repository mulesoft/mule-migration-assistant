/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.policy;

import static com.mulesoft.tools.migration.library.gateway.TestConstants.MIGRATION_RESOURCES_PATH;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.POLICY_APPLICATION_MODEL_PATH;
import static com.mulesoft.tools.migration.library.gateway.TestConstants.POLICY_EXAMPLES_PATH;
import static com.mulesoft.tools.migration.library.gateway.tasks.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.library.gateway.tasks.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.gateway.tasks.BasicStructureMigrationTask;
import com.mulesoft.tools.migration.library.gateway.tasks.MuleElementsMigrationTask;
import com.mulesoft.tools.migration.library.gateway.tasks.ThreatProtectionMigrationTask;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.MigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ThreatProtectionPolicyMigrationTestCase {

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
        {"xml-threat-protection-mule3-policy.xml", "xml-threat-protection-mule4-policy.xml", "xml-pom.xml"},
        {"json-threat-protection-mule3-policy.xml", "json-threat-protection-mule4-policy.xml", "json-pom.xml"},
    });
  }

  public ThreatProtectionPolicyMigrationTestCase(final String original, final String target, final String pom) {
    configPath = POLICY_EXAMPLES_PATH.resolve("original/" + original);
    targetPath = POLICY_EXAMPLES_PATH.resolve("expected/" + target);
    pomPath = POLICY_APPLICATION_MODEL_PATH.resolve(pom);
    reportMock = mock(MigrationReport.class);
  }

  @Before
  public void setUp() throws Exception {
    ApplicationModel.ApplicationModelBuilder amb = new ApplicationModel.ApplicationModelBuilder();
    amb.withProjectType(ProjectType.MULE_THREE_POLICY);
    amb.withProjectBasePath(POLICY_APPLICATION_MODEL_PATH);
    amb.withPom(pomPath);
    appModel = amb.build();

    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());

    ThreatProtectionMigrationTask task = new ThreatProtectionMigrationTask();
    task.setApplicationModel(appModel);
    steps = task.getSteps().stream()
        .filter(step -> step instanceof AbstractApplicationModelMigrationStep)
        .collect(toList());
    MuleElementsMigrationTask muleElementsMigrationTask = new MuleElementsMigrationTask();
    muleElementsMigrationTask.setApplicationModel(appModel);
    List<MigrationStep> policyUtilsMigrationSteps = muleElementsMigrationTask.getSteps().stream()
        .filter(step -> step instanceof AbstractApplicationModelMigrationStep)
        .collect(toList());
    steps.addAll(policyUtilsMigrationSteps);
    BasicStructureMigrationTask basicStructureTask = new BasicStructureMigrationTask();
    basicStructureTask.setApplicationModel(appModel);
    List<MigrationStep> basicStructureSteps = basicStructureTask.getSteps().stream()
        .filter(step -> step instanceof AbstractApplicationModelMigrationStep)
        .collect(toList());
    steps.addAll(basicStructureSteps);

  }

  private void migrate(AbstractApplicationModelMigrationStep migrationStep) {
    getElementsFromDocument(doc, migrationStep.getAppliedTo().getExpression())
        .forEach(node -> migrationStep.execute(node, reportMock));
  }

  @Test
  public void execute() throws Exception {
    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    steps.forEach(step -> migrate((AbstractApplicationModelMigrationStep) step));

    String xmlString = outputter.outputString(doc);
    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

  @After
  public void cleanup() throws Exception {
    File dwFile = POLICY_APPLICATION_MODEL_PATH.resolve(MIGRATION_RESOURCES_PATH).resolve("HttpListener.dwl").toFile();
    if (!dwFile.delete()) {
      dwFile.deleteOnExit();
    }
  }

}
