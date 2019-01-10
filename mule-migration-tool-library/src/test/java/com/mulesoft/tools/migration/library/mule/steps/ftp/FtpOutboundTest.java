/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.ftp;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.mule.steps.core.GenericGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveSyntheticMigrationAttributes;
import com.mulesoft.tools.migration.library.mule.steps.endpoint.OutboundEndpoint;
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
public class FtpOutboundTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path FTP_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/ftp");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "ftp-outbound-01",
        "ftp-outbound-02",
        "ftp-outbound-03",
        "ftp-outbound-04"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public FtpOutboundTest(String filePrefix) {
    configPath = FTP_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = FTP_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private GenericGlobalEndpoint genericGlobalEndpoint;
  private FtpGlobalEndpoint ftpGlobalEndpoint;
  private FtpEeGlobalEndpoint ftpEeGlobalEndpoint;
  private FtpConfig ftpConfig;
  private FtpEeConfig ftpEeConfig;
  private FtpOutboundEndpoint ftpOutboundEndpoint;
  private FtpEeOutboundEndpoint ftpEeOutboundEndpoint;
  private OutboundEndpoint outboundEndpoint;
  private RemoveSyntheticMigrationAttributes removeSyntheticMigrationAttributes;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    appModel = mockApplicationModel(doc, temp);

    genericGlobalEndpoint = new GenericGlobalEndpoint();
    genericGlobalEndpoint.setApplicationModel(appModel);

    MelToDwExpressionMigrator exprMigrator = new MelToDwExpressionMigrator(report.getReport(), appModel);

    ftpGlobalEndpoint = new FtpGlobalEndpoint();
    ftpGlobalEndpoint.setApplicationModel(appModel);
    ftpEeGlobalEndpoint = new FtpEeGlobalEndpoint();
    ftpEeGlobalEndpoint.setApplicationModel(appModel);
    ftpConfig = new FtpConfig();
    ftpConfig.setExpressionMigrator(exprMigrator);
    ftpConfig.setApplicationModel(appModel);
    ftpEeConfig = new FtpEeConfig();
    ftpEeConfig.setExpressionMigrator(exprMigrator);
    ftpEeConfig.setApplicationModel(appModel);
    ftpOutboundEndpoint = new FtpOutboundEndpoint();
    ftpOutboundEndpoint.setApplicationModel(appModel);
    ftpOutboundEndpoint.setExpressionMigrator(exprMigrator);
    ftpEeOutboundEndpoint = new FtpEeOutboundEndpoint();
    ftpEeOutboundEndpoint.setApplicationModel(appModel);
    ftpEeOutboundEndpoint.setExpressionMigrator(exprMigrator);
    outboundEndpoint = new OutboundEndpoint();
    outboundEndpoint.setExpressionMigrator(exprMigrator);
    outboundEndpoint.setApplicationModel(appModel);
    removeSyntheticMigrationAttributes = new RemoveSyntheticMigrationAttributes();
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, genericGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> genericGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, ftpGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> ftpGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, ftpEeGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> ftpEeGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, ftpConfig.getAppliedTo().getExpression())
        .forEach(node -> ftpConfig.execute(node, report.getReport()));
    getElementsFromDocument(doc, ftpEeConfig.getAppliedTo().getExpression())
        .forEach(node -> ftpEeConfig.execute(node, report.getReport()));
    getElementsFromDocument(doc, ftpOutboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> ftpOutboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, ftpEeOutboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> ftpEeOutboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, outboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> outboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, removeSyntheticMigrationAttributes.getAppliedTo().getExpression())
        .forEach(node -> removeSyntheticMigrationAttributes.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
