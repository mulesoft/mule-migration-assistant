/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.quartz;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.mule.steps.core.GenericGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveSyntheticMigrationAttributes;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
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
public class QuartzInboundTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path QUATZ_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/quartz");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "quartz-inbound-01",
        "quartz-inbound-02",
        "quartz-inbound-03",
        "quartz-inbound-04",
        "quartz-inbound-05",
        "quartz-inbound-06",
        "quartz-inbound-07",
        "quartz-inbound-08",
        "quartz-inbound-09",
        "quartz-inbound-10",
        "quartz-inbound-11",
        "quartz-inbound-12"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public QuartzInboundTest(String filePrefix) {
    configPath = QUATZ_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = QUATZ_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private GenericGlobalEndpoint genericGlobalEndpoint;
  private QuartzGlobalEndpoint quartzGlobalEndpoint;
  private QuartzInboundEndpoint quartzInboundEndpoint;
  private QuartzConnector quartzConnector;
  private RemoveSyntheticMigrationAttributes removeSyntheticMigrationAttributes;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    appModel = mockApplicationModel(doc, temp);

    MelToDwExpressionMigrator expressionMigrator = new MelToDwExpressionMigrator(report.getReport(), appModel);

    genericGlobalEndpoint = new GenericGlobalEndpoint();
    genericGlobalEndpoint.setApplicationModel(appModel);

    quartzGlobalEndpoint = new QuartzGlobalEndpoint();
    quartzGlobalEndpoint.setApplicationModel(appModel);
    quartzInboundEndpoint = new QuartzInboundEndpoint();
    // quartzInboundEndpoint.setExpressionMigrator(expressionMigrator);
    quartzInboundEndpoint.setApplicationModel(appModel);
    quartzConnector = new QuartzConnector();
    quartzConnector.setApplicationModel(appModel);

    removeSyntheticMigrationAttributes = new RemoveSyntheticMigrationAttributes();
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, genericGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> genericGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, quartzGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> quartzGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, quartzInboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> quartzInboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, quartzConnector.getAppliedTo().getExpression())
        .forEach(node -> quartzConnector.execute(node, report.getReport()));
    getElementsFromDocument(doc, removeSyntheticMigrationAttributes.getAppliedTo().getExpression())
        .forEach(node -> removeSyntheticMigrationAttributes.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
