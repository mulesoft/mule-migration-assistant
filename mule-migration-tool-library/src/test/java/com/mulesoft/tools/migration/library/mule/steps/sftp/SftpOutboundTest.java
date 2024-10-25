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
public class SftpOutboundTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path SFTP_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/sftp");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "sftp-outbound-01",
        // TODO MMT-260
        // "sftp-outbound-02",
        // "sftp-outbound-03",
        "sftp-outbound-04",
        "sftp-outbound-05",
        "sftp-outbound-06",
        "sftp-outbound-07",
        "sftp-outbound-08",
        "sftp-outbound-09",
        "sftp-outbound-10"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public SftpOutboundTest(String filePrefix) {
    configPath = SFTP_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = SFTP_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private GenericGlobalEndpoint genericGlobalEndpoint;
  private SftpGlobalEndpoint sftpGlobalEndpoint;
  private SftpConfig sftpConfig;
  private SftpOutboundEndpoint sftpOutboundEndpoint;
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

    sftpGlobalEndpoint = new SftpGlobalEndpoint();
    sftpGlobalEndpoint.setApplicationModel(appModel);
    sftpConfig = new SftpConfig();
    sftpConfig.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), appModel));
    sftpConfig.setApplicationModel(appModel);
    sftpOutboundEndpoint = new SftpOutboundEndpoint();
    sftpOutboundEndpoint.setApplicationModel(appModel);
    sftpOutboundEndpoint
        .setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), appModel));
    outboundEndpoint = new OutboundEndpoint();
    // inboundEndpoint.setExpressionMigrator(expressionMigrator);
    outboundEndpoint.setApplicationModel(appModel);
    removeSyntheticMigrationAttributes = new RemoveSyntheticMigrationAttributes();
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, genericGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> genericGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, sftpGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> sftpGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, sftpConfig.getAppliedTo().getExpression())
        .forEach(node -> sftpConfig.execute(node, report.getReport()));
    getElementsFromDocument(doc, sftpOutboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> sftpOutboundEndpoint.execute(node, report.getReport()));
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
