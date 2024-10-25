/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.tasks.basicstructure;

import static com.mulesoft.tools.migration.library.gateway.tasks.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.library.gateway.tasks.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.gateway.tasks.BasicStructureMigrationTask;
import com.mulesoft.tools.migration.project.ProjectType;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
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
public class BasicStructureMigrationTaskTestCase {

  private static final Path BASIC_POLICY_STRUCTURE_EXAMPLES_PATH =
      Paths.get("mule/apps/gateway/basic-policy-structure");

  private static final Path APPLICATION_MODEL_PATH =
      Paths.get("src/test/resources").resolve(BASIC_POLICY_STRUCTURE_EXAMPLES_PATH).resolve("original");

  private final Path configPath;
  private final Path targetPath;
  private final MigrationReport reportMock;
  private ApplicationModel appModel;
  private Document doc;

  private List<MigrationStep> steps;

  @Parameterized.Parameters(name = "{0}: {0}, {1}")
  public static Collection<Object[]> params() {
    return Arrays.asList(new Object[][] {
        {"policy-tag-original.xml", "policy-tag.xml"},
        {"non-matching-original.xml", "non-matching.xml"},
        {"before-tag-raw-original.xml", "before-tag-raw.xml"},
        {"before-tag-with-content-original.xml", "before-tag-with-content.xml"},
        {"after-tag-raw-original.xml", "after-tag-raw.xml"},
        {"after-tag-with-content-original.xml", "after-tag-with-content.xml"},
        {"before-after-tags-raw-original.xml", "before-after-tags-raw.xml"},
        {"before-after-tags-with-content-original.xml", "before-after-tags-with-content.xml"},
        {"after-before-tags-raw-original.xml", "after-before-tags-raw.xml"},
        {"after-before-tags-with-content-original.xml", "after-before-tags-with-content.xml"},
        {"before-exception-tag-raw-original.xml", "before-exception-tag-raw.xml"},
        {"before-exception-tag-with-content-original.xml", "before-exception-tag-with-content.xml"},
        {"after-exception-tag-raw-original.xml", "after-exception-tag-raw.xml"},
        {"after-exception-tag-with-content-original.xml", "after-exception-tag-with-content.xml"},
        {"pointcut-tag-raw-original.xml", "pointcut-tag-raw.xml"},
        {"pointcut-tag-with-content-original.xml", "pointcut-tag-with-content.xml"},
        {"namespaces-test.xml", "namespaces-test-result.xml"},

        {"a-ae-b-be-pc.xml", "full.xml"},
        {"a-ae-b-be.xml", "full.xml"},
        {"a-ae-b-pc-be.xml", "full.xml"},
        {"a-ae-b-pc.xml", "full.xml"},
        {"a-ae-b.xml", "full.xml"},
        {"a-ae-be-b-pc.xml", "full.xml"},
        {"a-ae-be-b.xml", "full.xml"},
        {"a-ae-be-pc-b.xml", "full.xml"},
        {"a-ae-be-pc.xml", "after-after-exception.xml"},
        {"a-ae-be.xml", "after-after-exception.xml"},
        {"a-ae-pc-b-be.xml", "full.xml"},
        {"a-ae-pc-b.xml", "full.xml"},
        {"a-ae-pc-be-b.xml", "full.xml"},
        {"a-ae-pc-be.xml", "after-after-exception.xml"},
        {"a-ae-pc.xml", "after-after-exception.xml"},
        {"a-ae.xml", "after-after-exception.xml"},
        {"a-b-ae-be-pc.xml", "full.xml"},
        {"a-b-ae-be.xml", "full.xml"},
        {"a-b-ae-pc-be.xml", "full.xml"},
        {"a-b-ae-pc.xml", "full.xml"},
        {"a-b-ae.xml", "full.xml"},
        {"a-b-be-ae-pc.xml", "full.xml"},
        {"a-b-be-ae.xml", "full.xml"},
        {"a-b-be-pc-ae.xml", "full.xml"},
        {"a-b-be-pc.xml", "before-after.xml"},
        {"a-b-be.xml", "before-after.xml"},
        {"a-b-pc-ae-be.xml", "full.xml"},
        {"a-b-pc-ae.xml", "full.xml"},
        {"a-b-pc-be-ae.xml", "full.xml"},
        {"a-b-pc-be.xml", "before-after.xml"},
        {"a-b-pc.xml", "before-after.xml"},
        {"a-b.xml", "before-after.xml"},
        {"a-be-ae-b-pc.xml", "full.xml"},
        {"a-be-ae-b.xml", "full.xml"},
        {"a-be-ae-pc-b.xml", "full.xml"},
        {"a-be-ae-pc.xml", "after-after-exception.xml"},
        {"a-be-ae.xml", "after-after-exception.xml"},
        {"a-be-b-ae-pc.xml", "full.xml"},
        {"a-be-b-ae.xml", "full.xml"},
        {"a-be-b-pc-ae.xml", "full.xml"},
        {"a-be-b-pc.xml", "before-after.xml"},
        {"a-be-b.xml", "before-after.xml"},
        {"a-be-pc-ae-b.xml", "full.xml"},
        {"a-be-pc-ae.xml", "after-after-exception.xml"},
        {"a-be-pc-b-ae.xml", "full.xml"},
        {"a-be-pc-b.xml", "before-after.xml"},
        {"a-be-pc.xml", "after.xml"},
        {"a-be.xml", "after.xml"},
        {"a-pc-ae-b-be.xml", "full.xml"},
        {"a-pc-ae-b.xml", "full.xml"},
        {"a-pc-ae-be-b.xml", "full.xml"},
        {"a-pc-ae-be.xml", "after-after-exception.xml"},
        {"a-pc-ae.xml", "after-after-exception.xml"},
        {"a-pc-b-ae-be.xml", "full.xml"},
        {"a-pc-b-ae.xml", "full.xml"},
        {"a-pc-b-be-ae.xml", "full.xml"},
        {"a-pc-b-be.xml", "before-after.xml"},
        {"a-pc-b.xml", "before-after.xml"},
        {"a-pc-be-ae-b.xml", "full.xml"},
        {"a-pc-be-ae.xml", "after-after-exception.xml"},
        {"a-pc-be-b-ae.xml", "full.xml"},
        {"a-pc-be-b.xml", "before-after.xml"},
        {"a-pc-be.xml", "after.xml"},
        {"a-pc.xml", "after.xml"},
        {"a.xml", "after.xml"},
        {"ae-a-b-be-pc.xml", "full.xml"},
        {"ae-a-b-be.xml", "full.xml"},
        {"ae-a-b-pc-be.xml", "full.xml"},
        {"ae-a-b-pc.xml", "full.xml"},
        {"ae-a-b.xml", "full.xml"},
        {"ae-a-be-b-pc.xml", "full.xml"},
        {"ae-a-be-b.xml", "full.xml"},
        {"ae-a-be-pc-b.xml", "full.xml"},
        {"ae-a-be-pc.xml", "after-after-exception.xml"},
        {"ae-a-be.xml", "after-after-exception.xml"},
        {"ae-a-pc-b-be.xml", "full.xml"},
        {"ae-a-pc-b.xml", "full.xml"},
        {"ae-a-pc-be-b.xml", "full.xml"},
        {"ae-a-pc-be.xml", "after-after-exception.xml"},
        {"ae-a-pc.xml", "after-after-exception.xml"},
        {"ae-a.xml", "after-after-exception.xml"},
        {"ae-b-a-be-pc.xml", "full.xml"},
        {"ae-b-a-be.xml", "full.xml"},
        {"ae-b-a-pc-be.xml", "full.xml"},
        {"ae-b-a-pc.xml", "full.xml"},
        {"ae-b-a.xml", "full.xml"},
        {"ae-b-be-a-pc.xml", "full.xml"},
        {"ae-b-be-a.xml", "full.xml"},
        {"ae-b-be-pc-a.xml", "full.xml"},
        {"ae-b-be-pc.xml", "after-exception-before.xml"},
        {"ae-b-be.xml", "after-exception-before.xml"},
        {"ae-b-pc-a-be.xml", "full.xml"},
        {"ae-b-pc-a.xml", "full.xml"},
        {"ae-b-pc-be-a.xml", "full.xml"},
        {"ae-b-pc-be.xml", "after-exception-before.xml"},
        {"ae-b-pc.xml", "after-exception-before.xml"},
        {"ae-b.xml", "after-exception-before.xml"},
        {"ae-be-a-b-pc.xml", "full.xml"},
        {"ae-be-a-b.xml", "full.xml"},
        {"ae-be-a-pc-b.xml", "full.xml"},
        {"ae-be-a-pc.xml", "after-after-exception.xml"},
        {"ae-be-a.xml", "after-after-exception.xml"},
        {"ae-be-b-a-pc.xml", "full.xml"},
        {"ae-be-b-a.xml", "full.xml"},
        {"ae-be-b-pc-a.xml", "full.xml"},
        {"ae-be-b-pc.xml", "after-exception-before.xml"},
        {"ae-be-b.xml", "after-exception-before.xml"},
        {"ae-be-pc-a-b.xml", "full.xml"},
        {"ae-be-pc-a.xml", "after-after-exception.xml"},
        {"ae-be-pc-b-a.xml", "full.xml"},
        {"ae-be-pc-b.xml", "after-exception-before.xml"},
        {"ae-be-pc.xml", "after-exception.xml"},
        {"ae-be.xml", "after-exception.xml"},
        {"ae-pc-a-b-be.xml", "full.xml"},
        {"ae-pc-a-b.xml", "full.xml"},
        {"ae-pc-a-be-b.xml", "full.xml"},
        {"ae-pc-a-be.xml", "after-after-exception.xml"},
        {"ae-pc-a.xml", "after-after-exception.xml"},
        {"ae-pc-b-a-be.xml", "full.xml"},
        {"ae-pc-b-a.xml", "full.xml"},
        {"ae-pc-b-be-a.xml", "full.xml"},
        {"ae-pc-b-be.xml", "after-exception-before.xml"},
        {"ae-pc-b.xml", "after-exception-before.xml"},
        {"ae-pc-be-a-b.xml", "full.xml"},
        {"ae-pc-be-a.xml", "after-after-exception.xml"},
        {"ae-pc-be-b-a.xml", "full.xml"},
        {"ae-pc-be-b.xml", "after-exception-before.xml"},
        {"ae-pc-be.xml", "after-exception.xml"},
        {"ae-pc.xml", "after-exception.xml"},
        {"ae.xml", "after-exception.xml"},
        {"b-a-ae-be-pc.xml", "full.xml"},
        {"b-a-ae-be.xml", "full.xml"},
        {"b-a-ae-pc-be.xml", "full.xml"},
        {"b-a-ae-pc.xml", "full.xml"},
        {"b-a-ae.xml", "full.xml"},
        {"b-a-be-ae-pc.xml", "full.xml"},
        {"b-a-be-ae.xml", "full.xml"},
        {"b-a-be-pc-ae.xml", "full.xml"},
        {"b-a-be-pc.xml", "before-after.xml"},
        {"b-a-be.xml", "before-after.xml"},
        {"b-a-pc-ae-be.xml", "full.xml"},
        {"b-a-pc-ae.xml", "full.xml"},
        {"b-a-pc-be-ae.xml", "full.xml"},
        {"b-a-pc-be.xml", "before-after.xml"},
        {"b-a-pc.xml", "before-after.xml"},
        {"b-a.xml", "before-after.xml"},
        {"b-ae-a-be-pc.xml", "full.xml"},
        {"b-ae-a-be.xml", "full.xml"},
        {"b-ae-a-pc-be.xml", "full.xml"},
        {"b-ae-a-pc.xml", "full.xml"},
        {"b-ae-a.xml", "full.xml"},
        {"b-ae-be-a-pc.xml", "full.xml"},
        {"b-ae-be-a.xml", "full.xml"},
        {"b-ae-be-pc-a.xml", "full.xml"},
        {"b-ae-be-pc.xml", "after-exception-before.xml"},
        {"b-ae-be.xml", "after-exception-before.xml"},
        {"b-ae-pc-a-be.xml", "full.xml"},
        {"b-ae-pc-a.xml", "full.xml"},
        {"b-ae-pc-be-a.xml", "full.xml"},
        {"b-ae-pc-be.xml", "after-exception-before.xml"},
        {"b-ae-pc.xml", "after-exception-before.xml"},
        {"b-ae.xml", "after-exception-before.xml"},
        {"b-be-a-ae-pc.xml", "full.xml"},
        {"b-be-a-ae.xml", "full.xml"},
        {"b-be-a-pc-ae.xml", "full.xml"},
        {"b-be-a-pc.xml", "before-after.xml"},
        {"b-be-a.xml", "before-after.xml"},
        {"b-be-ae-a-pc.xml", "full.xml"},
        {"b-be-ae-a.xml", "full.xml"},
        {"b-be-ae-pc-a.xml", "full.xml"},
        {"b-be-ae-pc.xml", "after-exception-before.xml"},
        {"b-be-ae.xml", "after-exception-before.xml"},
        {"b-be-pc-a-ae.xml", "full.xml"},
        {"b-be-pc-a.xml", "before-after.xml"},
        {"b-be-pc-ae-a.xml", "full.xml"},
        {"b-be-pc-ae.xml", "after-exception-before.xml"},
        {"b-be-pc.xml", "before.xml"},
        {"b-be.xml", "before.xml"},
        {"b-pc-a-ae-be.xml", "full.xml"},
        {"b-pc-a-ae.xml", "full.xml"},
        {"b-pc-a-be-ae.xml", "full.xml"},
        {"b-pc-a-be.xml", "before-after.xml"},
        {"b-pc-a.xml", "before-after.xml"},
        {"b-pc-ae-a-be.xml", "full.xml"},
        {"b-pc-ae-a.xml", "full.xml"},
        {"b-pc-ae-be-a.xml", "full.xml"},
        {"b-pc-ae-be.xml", "after-exception-before.xml"},
        {"b-pc-ae.xml", "after-exception-before.xml"},
        {"b-pc-be-a-ae.xml", "full.xml"},
        {"b-pc-be-a.xml", "before-after.xml"},
        {"b-pc-be-ae-a.xml", "full.xml"},
        {"b-pc-be-ae.xml", "after-exception-before.xml"},
        {"b-pc-be.xml", "before.xml"},
        {"b-pc.xml", "before.xml"},
        {"b.xml", "before.xml"},
        {"be-a-ae-b-pc.xml", "full.xml"},
        {"be-a-ae-b.xml", "full.xml"},
        {"be-a-ae-pc-b.xml", "full.xml"},
        {"be-a-ae-pc.xml", "after-after-exception.xml"},
        {"be-a-ae.xml", "after-after-exception.xml"},
        {"be-a-b-ae-pc.xml", "full.xml"},
        {"be-a-b-ae.xml", "full.xml"},
        {"be-a-b-pc-ae.xml", "full.xml"},
        {"be-a-b-pc.xml", "before-after.xml"},
        {"be-a-b.xml", "before-after.xml"},
        {"be-a-pc-ae-b.xml", "full.xml"},
        {"be-a-pc-ae.xml", "after-after-exception.xml"},
        {"be-a-pc-b-ae.xml", "full.xml"},
        {"be-a-pc-b.xml", "before-after.xml"},
        {"be-a-pc.xml", "after.xml"},
        {"be-a.xml", "after.xml"},
        {"be-ae-a-b-pc.xml", "full.xml"},
        {"be-ae-a-b.xml", "full.xml"},
        {"be-ae-a-pc-b.xml", "full.xml"},
        {"be-ae-a-pc.xml", "after-after-exception.xml"},
        {"be-ae-a.xml", "after-after-exception.xml"},
        {"be-ae-b-a-pc.xml", "full.xml"},
        {"be-ae-b-a.xml", "full.xml"},
        {"be-ae-b-pc-a.xml", "full.xml"},
        {"be-ae-b-pc.xml", "after-exception-before.xml"},
        {"be-ae-b.xml", "after-exception-before.xml"},
        {"be-ae-pc-a-b.xml", "full.xml"},
        {"be-ae-pc-a.xml", "after-after-exception.xml"},
        {"be-ae-pc-b-a.xml", "full.xml"},
        {"be-ae-pc-b.xml", "after-exception-before.xml"},
        {"be-ae-pc.xml", "after-exception.xml"},
        {"be-ae.xml", "after-exception.xml"},
        {"be-b-a-ae-pc.xml", "full.xml"},
        {"be-b-a-ae.xml", "full.xml"},
        {"be-b-a-pc-ae.xml", "full.xml"},
        {"be-b-a-pc.xml", "before-after.xml"},
        {"be-b-a.xml", "before-after.xml"},
        {"be-b-ae-a-pc.xml", "full.xml"},
        {"be-b-ae-a.xml", "full.xml"},
        {"be-b-ae-pc-a.xml", "full.xml"},
        {"be-b-ae-pc.xml", "after-exception-before.xml"},
        {"be-b-ae.xml", "after-exception-before.xml"},
        {"be-b-pc-a-ae.xml", "full.xml"},
        {"be-b-pc-a.xml", "before-after.xml"},
        {"be-b-pc-ae-a.xml", "full.xml"},
        {"be-b-pc-ae.xml", "after-exception-before.xml"},
        {"be-b-pc.xml", "before.xml"},
        {"be-b.xml", "before.xml"},
        {"be-pc-a-ae-b.xml", "full.xml"},
        {"be-pc-a-ae.xml", "after-after-exception.xml"},
        {"be-pc-a-b-ae.xml", "full.xml"},
        {"be-pc-a-b.xml", "before-after.xml"},
        {"be-pc-a.xml", "after.xml"},
        {"be-pc-ae-a-b.xml", "full.xml"},
        {"be-pc-ae-a.xml", "after-after-exception.xml"},
        {"be-pc-ae-b-a.xml", "full.xml"},
        {"be-pc-ae-b.xml", "after-exception-before.xml"},
        {"be-pc-ae.xml", "after-exception.xml"},
        {"be-pc-b-a-ae.xml", "full.xml"},
        {"be-pc-b-a.xml", "before-after.xml"},
        {"be-pc-b-ae-a.xml", "full.xml"},
        {"be-pc-b-ae.xml", "after-exception-before.xml"},
        {"be-pc-b.xml", "before.xml"},
        {"be-pc.xml", "raw.xml"},
        {"be.xml", "raw.xml"},
        {"pc-a-ae-b-be.xml", "full.xml"},
        {"pc-a-ae-b.xml", "full.xml"},
        {"pc-a-ae-be-b.xml", "full.xml"},
        {"pc-a-ae-be.xml", "after-after-exception.xml"},
        {"pc-a-ae.xml", "after-after-exception.xml"},
        {"pc-a-b-ae-be.xml", "full.xml"},
        {"pc-a-b-ae.xml", "full.xml"},
        {"pc-a-b-be-ae.xml", "full.xml"},
        {"pc-a-b-be.xml", "before-after.xml"},
        {"pc-a-b.xml", "before-after.xml"},
        {"pc-a-be-ae-b.xml", "full.xml"},
        {"pc-a-be-ae.xml", "after-after-exception.xml"},
        {"pc-a-be-b-ae.xml", "full.xml"},
        {"pc-a-be-b.xml", "before-after.xml"},
        {"pc-a-be.xml", "after.xml"},
        {"pc-a.xml", "after.xml"},
        {"pc-ae-a-b-be.xml", "full.xml"},
        {"pc-ae-a-b.xml", "full.xml"},
        {"pc-ae-a-be-b.xml", "full.xml"},
        {"pc-ae-a-be.xml", "after-after-exception.xml"},
        {"pc-ae-a.xml", "after-after-exception.xml"},
        {"pc-ae-b-a-be.xml", "full.xml"},
        {"pc-ae-b-a.xml", "full.xml"},
        {"pc-ae-b-be-a.xml", "full.xml"},
        {"pc-ae-b-be.xml", "after-exception-before.xml"},
        {"pc-ae-b.xml", "after-exception-before.xml"},
        {"pc-ae-be-a-b.xml", "full.xml"},
        {"pc-ae-be-a.xml", "after-after-exception.xml"},
        {"pc-ae-be-b-a.xml", "full.xml"},
        {"pc-ae-be-b.xml", "after-exception-before.xml"},
        {"pc-ae-be.xml", "after-exception.xml"},
        {"pc-ae.xml", "after-exception.xml"},
        {"pc-b-a-ae-be.xml", "full.xml"},
        {"pc-b-a-ae.xml", "full.xml"},
        {"pc-b-a-be-ae.xml", "full.xml"},
        {"pc-b-a-be.xml", "before-after.xml"},
        {"pc-b-a.xml", "before-after.xml"},
        {"pc-b-ae-a-be.xml", "full.xml"},
        {"pc-b-ae-a.xml", "full.xml"},
        {"pc-b-ae-be-a.xml", "full.xml"},
        {"pc-b-ae-be.xml", "after-exception-before.xml"},
        {"pc-b-ae.xml", "after-exception-before.xml"},
        {"pc-b-be-a-ae.xml", "full.xml"},
        {"pc-b-be-a.xml", "before-after.xml"},
        {"pc-b-be-ae-a.xml", "full.xml"},
        {"pc-b-be-ae.xml", "after-exception-before.xml"},
        {"pc-b-be.xml", "before.xml"},
        {"pc-b.xml", "before.xml"},
        {"pc-be-a-ae-b.xml", "full.xml"},
        {"pc-be-a-ae.xml", "after-after-exception.xml"},
        {"pc-be-a-b-ae.xml", "full.xml"},
        {"pc-be-a-b.xml", "before-after.xml"},
        {"pc-be-a.xml", "after.xml"},
        {"pc-be-ae-a-b.xml", "full.xml"},
        {"pc-be-ae-a.xml", "after-after-exception.xml"},
        {"pc-be-ae-b-a.xml", "full.xml"},
        {"pc-be-ae-b.xml", "after-exception-before.xml"},
        {"pc-be-ae.xml", "after-exception.xml"},
        {"pc-be-b-a-ae.xml", "full.xml"},
        {"pc-be-b-a.xml", "before-after.xml"},
        {"pc-be-b-ae-a.xml", "full.xml"},
        {"pc-be-b-ae.xml", "after-exception-before.xml"},
        {"pc-be-b.xml", "before.xml"},
        {"pc-be.xml", "raw.xml"},
        {"pc.xml", "raw.xml"},
    });
  }

  public BasicStructureMigrationTaskTestCase(final String original, final String target) {
    configPath = BASIC_POLICY_STRUCTURE_EXAMPLES_PATH.resolve("original/" + original);
    targetPath = BASIC_POLICY_STRUCTURE_EXAMPLES_PATH.resolve("expected/" + target);
    reportMock = mock(MigrationReport.class);
  }

  @Before
  public void setUp() throws Exception {
    ApplicationModel.ApplicationModelBuilder appModelBuilder = new ApplicationModel.ApplicationModelBuilder();
    appModelBuilder.withProjectType(ProjectType.MULE_THREE_POLICY);
    appModelBuilder.withProjectBasePath(APPLICATION_MODEL_PATH);
    appModel = appModelBuilder.build();

    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    steps = new BasicStructureMigrationTask().getSteps();
  }

  private void migrate(MigrationStep migrationStep) {
    if (migrationStep instanceof AbstractApplicationModelMigrationStep) {
      getElementsFromDocument(doc, ((AbstractApplicationModelMigrationStep) migrationStep).getAppliedTo().getExpression())
          .forEach(node -> migrationStep.execute(node, reportMock));
    }
  }

  @Test
  public void execute() throws Exception {
    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    steps.forEach(step -> migrate(step));

    String xmlString = outputter.outputString(doc);
    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
