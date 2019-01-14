/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
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
        {"collection-splitter-aggregator-02", asList("splitter.attributes.neverCorrelation")},
        {"collection-splitter-aggregator-03", asList("splitter.aggregator.missing")},
        {"collection-splitter-aggregator-04", emptyList()},
        {"collection-splitter-aggregator-05", emptyList()},
        {"collection-splitter-aggregator-06", asList("splitter.aggregator.processedGroupsObjectStore")},
        {"collection-splitter-aggregator-07", asList("splitter.aggregator.eventGroupsObjectStore")},
        {"collection-splitter-aggregator-08", asList("splitter.aggregator.persistentStores")},
        {"collection-splitter-aggregator-09", asList("splitter.aggregator.storePrefix")},
        {"collection-splitter-aggregator-10", asList("splitter.aggregator.missing", "splitter.aggregator.noSplitter")},
        {"expression-splitter-aggregator-01", asList("splitter.attributes.evaluator")},
        {"expression-splitter-aggregator-02", asList("splitter.attributes.evaluator", "splitter.attributes.customEvaluator")},
        {"expression-splitter-aggregator-03", emptyList()},
        {"multiple-splitter-aggregator-01", emptyList()},
        {"multiple-splitter-aggregator-02", emptyList()},
        {"splitter-custom-aggregator-01", asList("splitter.aggregator.custom")}
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

  private AbstractSplitter collectionSplitter;
  private AbstractSplitter expressionSplitter;
  private AggregatorWithNoSplitter aggregatorWithNoSplitter;
  private VmNamespaceContribution vmNamespaceContribution;
  private AggregatorsNamespaceContribution aggregatorsNamespaceContribution;
  private RemoveSyntheticMigrationGlobalElements removeSyntheticMigrationGlobalElements;
  private ApplicationModel applicationModel;

  @Before
  public void setUp() throws Exception {
    buildProject();
    applicationModel = generateAppModel(fileUnderTestPath);

    expressionMigrator = new MelToDwExpressionMigrator(report.getReport(), applicationModel);

    collectionSplitter = new CollectionSplitter();
    collectionSplitter.setApplicationModel(applicationModel);
    collectionSplitter.setExpressionMigrator(expressionMigrator);

    expressionSplitter = new ExpressionSplitter();
    expressionSplitter.setApplicationModel(applicationModel);
    expressionSplitter.setExpressionMigrator(expressionMigrator);

    aggregatorWithNoSplitter = new AggregatorWithNoSplitter();
    aggregatorWithNoSplitter.setApplicationModel(applicationModel);

    vmNamespaceContribution = new VmNamespaceContribution();
    aggregatorsNamespaceContribution = new AggregatorsNamespaceContribution();
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

    getElementsFromDocument(document, aggregatorWithNoSplitter.getAppliedTo().getExpression())
        .forEach(node -> aggregatorWithNoSplitter.execute(node, report.getReport()));

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
