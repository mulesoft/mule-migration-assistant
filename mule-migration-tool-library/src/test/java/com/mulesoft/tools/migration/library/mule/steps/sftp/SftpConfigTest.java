/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.sftp;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

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
public class SftpConfigTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path SFTP_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/sftp");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "sftp-config-01",
        "sftp-config-02",
        "sftp-config-03",
        "sftp-config-04",
        "sftp-config-05",
        "sftp-config-06",
        "sftp-config-07",
        "sftp-config-08",
        "sftp-config-09",
        "sftp-config-10",
        "sftp-config-11",
        "sftp-config-12",
        "sftp-config-13",
        "sftp-config-14",
        "sftp-config-15"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public SftpConfigTest(String filePrefix) {
    configPath = SFTP_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = SFTP_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private SftpGlobalEndpoint sftpGlobalEndpoint;
  private SftpConfig sftpConfig;
  private SftpInboundEndpoint sftpInboundEndpoint;
  private SftpOutboundEndpoint sftpOutboundEndpoint;
  private RemoveSyntheticMigrationAttributes removeSyntheticMigrationAttributes;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    appModel = mockApplicationModel(doc, temp);

    sftpGlobalEndpoint = new SftpGlobalEndpoint();
    sftpGlobalEndpoint.setApplicationModel(appModel);
    sftpConfig = new SftpConfig();
    sftpConfig.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), appModel));
    sftpConfig.setApplicationModel(appModel);
    sftpInboundEndpoint = new SftpInboundEndpoint();
    sftpInboundEndpoint.setApplicationModel(appModel);
    sftpOutboundEndpoint = new SftpOutboundEndpoint();
    sftpOutboundEndpoint.setApplicationModel(appModel);
    sftpOutboundEndpoint
        .setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), appModel));
    removeSyntheticMigrationAttributes = new RemoveSyntheticMigrationAttributes();
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, sftpGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> sftpGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, sftpConfig.getAppliedTo().getExpression())
        .forEach(node -> sftpConfig.execute(node, report.getReport()));
    getElementsFromDocument(doc, sftpInboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> sftpInboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, sftpOutboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> sftpOutboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, removeSyntheticMigrationAttributes.getAppliedTo().getExpression())
        .forEach(node -> removeSyntheticMigrationAttributes.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
