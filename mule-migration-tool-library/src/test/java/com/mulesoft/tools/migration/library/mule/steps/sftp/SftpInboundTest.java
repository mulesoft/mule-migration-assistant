/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.sftp;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.mule.steps.core.CatchExceptionStrategy;
import com.mulesoft.tools.migration.library.mule.steps.core.GenericGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveSyntheticMigrationAttributes;
import com.mulesoft.tools.migration.library.mule.steps.core.RollbackExceptionStrategy;
import com.mulesoft.tools.migration.library.mule.steps.endpoint.InboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.quartz.QuartzGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.quartz.QuartzInboundEndpoint;
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
public class SftpInboundTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path SFTP_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/sftp");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "sftp-inbound-01",
        "sftp-inbound-02",
        "sftp-inbound-03",
        "sftp-inbound-04",
        "sftp-inbound-05",
        "sftp-inbound-06",
        "sftp-inbound-07",
        "sftp-inbound-08",
        "sftp-inbound-09",
        "sftp-inbound-10",
        "sftp-inbound-11",
        "sftp-inbound-12",
        "sftp-inbound-13",
        "sftp-inbound-14",
        "sftp-inbound-15",
        "sftp-inbound-16",
        "sftp-inbound-17",
        "sftp-inbound-18",
        "sftp-inbound-19",
        "sftp-inbound-20",
        "sftp-inbound-21"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public SftpInboundTest(String filePrefix) {
    configPath = SFTP_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = SFTP_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private CatchExceptionStrategy catchExceptionStrategy;
  private RollbackExceptionStrategy rollbackExceptionStrategy;
  private GenericGlobalEndpoint genericGlobalEndpoint;
  private QuartzGlobalEndpoint quartzGlobalEndpoint;
  private QuartzInboundEndpoint quartzInboundEndpoint;
  private SftpGlobalEndpoint sftpGlobalEndpoint;
  private SftpConfig sftpConfig;
  private SftpInboundEndpoint sftpInboundEndpoint;
  private InboundEndpoint inboundEndpoint;
  private RemoveSyntheticMigrationAttributes removeSyntheticMigrationAttributes;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    appModel = mockApplicationModel(doc, temp);

    MelToDwExpressionMigrator expressionMigrator = new MelToDwExpressionMigrator(report.getReport(), appModel);

    catchExceptionStrategy = new CatchExceptionStrategy();
    rollbackExceptionStrategy = new RollbackExceptionStrategy();

    genericGlobalEndpoint = new GenericGlobalEndpoint();
    genericGlobalEndpoint.setApplicationModel(appModel);

    quartzGlobalEndpoint = new QuartzGlobalEndpoint();
    quartzGlobalEndpoint.setApplicationModel(appModel);
    quartzInboundEndpoint = new QuartzInboundEndpoint();
    // quartzInboundEndpoint.setExpressionMigrator(expressionMigrator);
    quartzInboundEndpoint.setApplicationModel(appModel);
    sftpGlobalEndpoint = new SftpGlobalEndpoint();
    sftpGlobalEndpoint.setApplicationModel(appModel);
    sftpConfig = new SftpConfig();
    sftpConfig.setExpressionMigrator(expressionMigrator);
    sftpConfig.setApplicationModel(appModel);
    sftpInboundEndpoint = new SftpInboundEndpoint();
    sftpInboundEndpoint.setExpressionMigrator(expressionMigrator);
    sftpInboundEndpoint.setApplicationModel(appModel);
    inboundEndpoint = new InboundEndpoint();
    inboundEndpoint.setExpressionMigrator(expressionMigrator);
    inboundEndpoint.setApplicationModel(appModel);
    removeSyntheticMigrationAttributes = new RemoveSyntheticMigrationAttributes();
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, catchExceptionStrategy.getAppliedTo().getExpression())
        .forEach(node -> catchExceptionStrategy.execute(node, report.getReport()));
    getElementsFromDocument(doc, rollbackExceptionStrategy.getAppliedTo().getExpression())
        .forEach(node -> rollbackExceptionStrategy.execute(node, report.getReport()));

    getElementsFromDocument(doc, quartzGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> quartzGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, quartzInboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> quartzInboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, sftpGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> sftpGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, sftpGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> sftpGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, sftpConfig.getAppliedTo().getExpression())
        .forEach(node -> sftpConfig.execute(node, report.getReport()));
    getElementsFromDocument(doc, sftpInboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> sftpInboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, inboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> inboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, removeSyntheticMigrationAttributes.getAppliedTo().getExpression())
        .forEach(node -> removeSyntheticMigrationAttributes.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
