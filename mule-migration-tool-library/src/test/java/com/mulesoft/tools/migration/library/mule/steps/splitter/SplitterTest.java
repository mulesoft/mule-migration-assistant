/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.splitter;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.utils.ApplicationModelUtils.generateAppModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.google.common.collect.Iterables;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveSyntheticMigrationAttributes;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveSyntheticMigrationGlobalElements;
import com.mulesoft.tools.migration.library.mule.steps.vm.VmNamespaceContribution;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.tck.ReportVerification;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
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


@RunWith(Parameterized.class)
public class SplitterTest {

  private static final Path SPLITTER_EXAMPLE_PATHS = Paths.get("mule/apps/splitter-aggregator");
  private static final String DUMMY_APP_NAME = "splitter-aggregator-app";

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return asList(new Object[][] {
        {"collection-splitter-aggregator-01", emptyList()},
        {"collection-splitter-aggregator-02", asList("splitter.neverCorrelationAttribute")},
        {"collection-splitter-aggregator-03", asList("aggregator.missing")},
        {"collection-splitter-aggregator-04", emptyList()},
        {"collection-splitter-aggregator-05", emptyList()},
        {"collection-splitter-aggregator-06", asList("aggregator.processedGroupsObjectStore")},
        {"collection-splitter-aggregator-07", asList("aggregator.eventGroupsObjectStore")},
        {"collection-splitter-aggregator-08", asList("aggregator.persistentStores")},
        {"collection-splitter-aggregator-09", asList("aggregator.storePrefix")},
        {"collection-splitter-aggregator-10", asList("aggregator.missing", "aggregator.noSplitter")},
        {"custom-splitter-aggregator-01", asList("splitter.custom", "aggregator.customSplitter")},
        {"expression-splitter-aggregator-01", asList("splitter.evaluatorAttribute")},
        {"expression-splitter-aggregator-02", asList("splitter.evaluatorAttribute", "splitter.customEvaluatorAttribute")},
        {"expression-splitter-aggregator-03", emptyList()},
        {"map-splitter-aggregator-01", emptyList()},
        {"message-chunk-splitter-aggregator-01", asList("splitter.messageChunk", "aggregator.messageChunk")},
        {"multiple-splitter-aggregator-01", emptyList()},
        {"multiple-splitter-aggregator-02", emptyList()},
        {"multiple-splitter-aggregator-03", asList("aggregator.missing",
                                                   "aggregator.custom",
                                                   "aggregator.noSplitter",
                                                   "splitter.custom",
                                                   "aggregator.customSplitter",
                                                   "splitter.messageChunk",
                                                   "aggregator.messageChunk")},
        {"splitter-custom-aggregator-01", asList("aggregator.custom")}
    });
  }

  private final Path configPath;
  private final Path targetPath;
  private Path fileUnderTestPath;
  private List<String> expectedReportKeys;
  private ExpressionMigrator expressionMigrator;

  public SplitterTest(String filePrefix, List<String> expectedReportKeys) {
    configPath = SPLITTER_EXAMPLE_PATHS.resolve(filePrefix + "-original.xml");
    targetPath = SPLITTER_EXAMPLE_PATHS.resolve(filePrefix + ".xml");
    this.expectedReportKeys = expectedReportKeys;
  }

  private CollectionSplitter collectionSplitter;
  private ExpressionSplitter expressionSplitter;
  private CustomSplitter customSplitter;
  private MapSplitter mapSplitter;
  private MessageChunkSplitter messageChunkSplitter;
  private AggregatorWithNoSplitter aggregatorWithNoSplitter;
  private VmNamespaceContribution vmNamespaceContribution;
  private AggregatorsNamespaceContribution aggregatorsNamespaceContribution;
  private RemoveSyntheticMigrationAttributes removeSyntheticMigrationAttributes;
  private RemoveSyntheticMigrationGlobalElements removeSyntheticMigrationGlobalElements;
  private ApplicationModel applicationModel;

  @Before
  public void setUp() throws Exception {
    buildProject();
    applicationModel = generateAppModel(fileUnderTestPath);

    expressionMigrator = new MelToDwExpressionMigrator(report.getReport(), applicationModel);

    collectionSplitter = new CollectionSplitter();
    collectionSplitter.setApplicationModel(applicationModel);

    expressionSplitter = new ExpressionSplitter();
    expressionSplitter.setApplicationModel(applicationModel);
    expressionSplitter.setExpressionMigrator(expressionMigrator);

    customSplitter = new CustomSplitter();
    customSplitter.setApplicationModel(applicationModel);

    mapSplitter = new MapSplitter();
    mapSplitter.setApplicationModel(applicationModel);

    messageChunkSplitter = new MessageChunkSplitter();
    messageChunkSplitter.setApplicationModel(applicationModel);

    aggregatorWithNoSplitter = new AggregatorWithNoSplitter();
    aggregatorWithNoSplitter.setApplicationModel(applicationModel);

    vmNamespaceContribution = new VmNamespaceContribution();
    aggregatorsNamespaceContribution = new AggregatorsNamespaceContribution();
    removeSyntheticMigrationAttributes = new RemoveSyntheticMigrationAttributes();
    removeSyntheticMigrationGlobalElements = new RemoveSyntheticMigrationGlobalElements();

    for (String expectedReportKey : expectedReportKeys) {
      report.expectReportEntry(expectedReportKey);
    }
  }

  private void buildProject() throws IOException {
    fileUnderTestPath = temporaryFolder.newFolder(DUMMY_APP_NAME).toPath();
    File app = fileUnderTestPath.resolve("src").resolve("main").resolve("app").toFile();
    app.mkdirs();

    URL sample = this.getClass().getClassLoader().getResource(configPath.toString());
    FileUtils.copyURLToFile(sample, new File(app, configPath.getFileName().toString()));
  }

  @Test
  public void execute() throws Exception {
    Document document = Iterables.get(applicationModel.getApplicationDocuments().values(), 0);

    vmNamespaceContribution.execute(applicationModel, report.getReport());
    aggregatorsNamespaceContribution.execute(applicationModel, report.getReport());

    getElementsFromDocument(document, collectionSplitter.getAppliedTo().getExpression())
        .forEach(node -> collectionSplitter.execute(node, report.getReport()));

    getElementsFromDocument(document, expressionSplitter.getAppliedTo().getExpression())
        .forEach(node -> expressionSplitter.execute(node, report.getReport()));

    getElementsFromDocument(document, customSplitter.getAppliedTo().getExpression())
        .forEach(node -> customSplitter.execute(node, report.getReport()));

    getElementsFromDocument(document, mapSplitter.getAppliedTo().getExpression())
        .forEach(node -> mapSplitter.execute(node, report.getReport()));

    getElementsFromDocument(document, messageChunkSplitter.getAppliedTo().getExpression())
        .forEach(node -> messageChunkSplitter.execute(node, report.getReport()));

    getElementsFromDocument(document, aggregatorWithNoSplitter.getAppliedTo().getExpression())
        .forEach(node -> aggregatorWithNoSplitter.execute(node, report.getReport()));

    getElementsFromDocument(document, removeSyntheticMigrationAttributes.getAppliedTo().getExpression())
        .forEach(node -> removeSyntheticMigrationAttributes.execute(node, report.getReport()));

    getElementsFromDocument(document, removeSyntheticMigrationGlobalElements.getAppliedTo().getExpression())
        .forEach(node -> removeSyntheticMigrationGlobalElements.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(document);

    assertThat(xmlString,
               isSimilarTo(IOUtils
                   .toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                       .ignoreComments().normalizeWhitespace());
  }
}
