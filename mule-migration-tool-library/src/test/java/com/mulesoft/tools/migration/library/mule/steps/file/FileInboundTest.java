/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.file;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.library.mule.steps.core.GenericGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveSyntheticMigrationAttributes;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.CustomFilter;
import com.mulesoft.tools.migration.library.mule.steps.endpoint.InboundEndpoint;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
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
public class FileInboundTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path FILE_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/file");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "file-inbound-01",
        "file-inbound-02",
        "file-inbound-03",
        "file-inbound-04",
        "file-inbound-05",
        "file-inbound-06",
        "file-inbound-07",
        "file-inbound-08",
        "file-inbound-09",
        "file-inbound-10",
        "file-inbound-11",
        "file-inbound-12",
        "file-inbound-13",
        "file-inbound-14",
        "file-inbound-15",
        "file-inbound-16",
        "file-inbound-17",
        "file-inbound-18",
        "file-inbound-19"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public FileInboundTest(String filePrefix) {
    configPath = FILE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = FILE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private GenericGlobalEndpoint genericGlobalEndpoint;
  private CustomFilter customFilter;
  private FileGlobalEndpoint fileGlobalEndpoint;
  private FileConfig fileConfig;
  private FileInboundEndpoint fileInboundEndpoint;
  private FileTransformers fileTransformers;
  private InboundEndpoint inboundEndpoint;
  private RemoveSyntheticMigrationAttributes removeSyntheticMigrationAttributes;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    appModel = mockApplicationModel(doc, temp);

    MelToDwExpressionMigrator expressionMigrator = new MelToDwExpressionMigrator(report.getReport(), appModel);

    customFilter = new CustomFilter();
    customFilter.setApplicationModel(appModel);
    genericGlobalEndpoint = new GenericGlobalEndpoint();
    genericGlobalEndpoint.setApplicationModel(appModel);

    fileGlobalEndpoint = new FileGlobalEndpoint();
    fileGlobalEndpoint.setApplicationModel(appModel);
    fileConfig = new FileConfig();
    fileConfig.setExpressionMigrator(expressionMigrator);
    fileConfig.setApplicationModel(appModel);
    fileInboundEndpoint = new FileInboundEndpoint();
    fileInboundEndpoint.setExpressionMigrator(expressionMigrator);
    fileInboundEndpoint.setApplicationModel(appModel);
    fileTransformers = new FileTransformers();
    inboundEndpoint = new InboundEndpoint();
    inboundEndpoint.setExpressionMigrator(expressionMigrator);
    inboundEndpoint.setApplicationModel(appModel);
    removeSyntheticMigrationAttributes = new RemoveSyntheticMigrationAttributes();
  }

  @Ignore
  @Test(expected = MigrationStepException.class)
  public void executeWithNullElement() throws Exception {
    fileConfig.execute(null, report.getReport());
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, genericGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> genericGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, fileGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> fileGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, fileConfig.getAppliedTo().getExpression())
        .forEach(node -> fileConfig.execute(node, report.getReport()));
    getElementsFromDocument(doc, fileInboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> fileInboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, fileTransformers.getAppliedTo().getExpression())
        .forEach(node -> fileTransformers.execute(node, report.getReport()));
    getElementsFromDocument(doc, inboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> inboundEndpoint.execute(node, report.getReport()));

    getElementsFromDocument(doc, customFilter.getAppliedTo().getExpression())
        .forEach(node -> customFilter.execute(node, report.getReport()));
    getElementsFromDocument(doc, removeSyntheticMigrationAttributes.getAppliedTo().getExpression())
        .forEach(node -> removeSyntheticMigrationAttributes.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
