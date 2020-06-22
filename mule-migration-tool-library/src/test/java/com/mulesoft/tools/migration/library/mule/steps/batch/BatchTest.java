/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.batch;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.tck.ReportVerification;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(Parameterized.class)
public class BatchTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path BATCH_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/batch");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "batch-01",
        "batch-02",
        "batch-03",
        "batch-04",
        "batch-05",
        "batch-06",
        "batch-07",
        "batch-08",
        "batch-09",
        "batch-10",
        "batch-11",
        "batch-12",
        "batch-13"
    };
  }

  private final Path configPath;
  private final Path targetPath;
  private BatchJob batchJob;
  private BatchExecute batchExecute;
  private BatchSetRecordVariable batchSetRecordVariable;
  private BatchCommit batchCommit;
  private BatchStep batchStep;
  private BatchHistoryExpiration batchHistoryExpiration;
  private Document doc;
  private ApplicationModel appModel;

  public BatchTest(String filePrefix) {
    configPath = BATCH_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = BATCH_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    appModel = mockApplicationModel(doc, temp);

    batchJob = new BatchJob();
    batchExecute = new BatchExecute();
    batchSetRecordVariable = new BatchSetRecordVariable();
    batchCommit = new BatchCommit();
    batchStep = new BatchStep();
    batchHistoryExpiration = new BatchHistoryExpiration();

    MelToDwExpressionMigrator expressionMigrator = new MelToDwExpressionMigrator(report.getReport(), appModel);
    batchSetRecordVariable.setExpressionMigrator(expressionMigrator);
    batchExecute.setExpressionMigrator(expressionMigrator);
    batchStep.setExpressionMigrator(expressionMigrator);
    batchJob.setExpressionMigrator(expressionMigrator);
  }

  public void migrate(AbstractApplicationModelMigrationStep migrationStep) {
    getElementsFromDocument(doc, migrationStep.getAppliedTo().getExpression())
        .forEach(node -> migrationStep.execute(node, report.getReport()));
  }

  @Test
  public void execute() throws Exception {
    migrate(batchExecute);
    migrate(batchJob);
    migrate(batchSetRecordVariable);
    migrate(batchStep);
    migrate(batchCommit);
    migrate(batchHistoryExpiration);

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
