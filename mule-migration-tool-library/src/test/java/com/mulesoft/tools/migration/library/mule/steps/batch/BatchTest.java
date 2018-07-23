/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.batch;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.library.mule.steps.core.GenericGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.CustomFilter;
import com.mulesoft.tools.migration.library.mule.steps.endpoint.InboundEndpoint;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Ignore;
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

  private static final Path BATCH_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/batch");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "batch-01",
        "batch-02",
        "batch-03",
        "batch-04"
    };
  }

  private final Path configPath;
  private final Path targetPath;
  private final MigrationReport reportMock;
  private BatchJob batchJob;
  private BatchExecute batchExecute;
  private BatchSetRecordVariable batchSetRecordVariable;
  private BatchCommit batchCommit;
  private Document doc;
  private ApplicationModel appModel;

  public BatchTest(String filePrefix) {
    configPath = BATCH_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = BATCH_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
    reportMock = mock(MigrationReport.class);
  }

  @Before
  public void setUp() throws Exception {
    appModel = mock(ApplicationModel.class);
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    batchJob = new BatchJob();
    batchExecute = new BatchExecute();
    batchSetRecordVariable = new BatchSetRecordVariable();
    batchCommit = new BatchCommit();

    MelToDwExpressionMigrator expressionMigrator = new MelToDwExpressionMigrator(reportMock, mock(ApplicationModel.class));
    batchSetRecordVariable.setExpressionMigrator(expressionMigrator);
    appModel = mock(ApplicationModel.class);
    when(appModel.getNodes(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc, (String) invocation.getArguments()[0]));
    when(appModel.getProjectBasePath()).thenReturn(temp.newFolder().toPath());

  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, batchExecute.getAppliedTo().getExpression())
        .forEach(node -> batchExecute.execute(node, reportMock));
    getElementsFromDocument(doc, batchJob.getAppliedTo().getExpression()).forEach(node -> batchJob.execute(node, reportMock));
    getElementsFromDocument(doc, batchSetRecordVariable.getAppliedTo().getExpression())
        .forEach(node -> batchSetRecordVariable.execute(node, reportMock));
    getElementsFromDocument(doc, batchCommit.getAppliedTo().getExpression())
        .forEach(node -> batchCommit.execute(node, reportMock));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
