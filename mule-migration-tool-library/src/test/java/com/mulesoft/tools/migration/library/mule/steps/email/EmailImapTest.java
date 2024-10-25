/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.email;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.mule.steps.core.GenericGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.core.MessageAttachmentsListExpressionEvaluator;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(Parameterized.class)
public class EmailImapTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path EMAIL_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/email");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "email-imap-inbound-01",
        "email-imap-inbound-02",
        "email-imap-inbound-03",
        "email-imap-inbound-04",
        "email-imap-inbound-05",
        "email-imap-inbound-06",
        "email-imap-inbound-07",
        "email-imap-inbound-08",
        "email-imap-inbound-09",
        "email-imap-inbound-10",
        "email-imap-inbound-11",
        "gmail-imap-inbound-01"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public EmailImapTest(String filePrefix) {
    configPath = EMAIL_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = EMAIL_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private GenericGlobalEndpoint genericGlobalEndpoint;
  private CustomFilter customFilter;
  private ImapGlobalEndpoint imapGlobalEndpoint;
  private ImapsGlobalEndpoint imapsGlobalEndpoint;
  private ImapInboundEndpoint imapInboundEndpoint;
  private ImapsInboundEndpoint imapsInboundEndpoint;
  private EmailTransformers emailTransformers;
  private EmailConnectorConfig emailConfig;
  private InboundEndpoint inboundEndpoint;
  private MessageAttachmentsListExpressionEvaluator messageAttachmentsListExpressionEvaluator;
  private RemoveSyntheticMigrationAttributes removeSyntheticMigrationAttributes;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    appModel = mockApplicationModel(doc, temp);

    customFilter = new CustomFilter();

    MelToDwExpressionMigrator expressionMigrator = new MelToDwExpressionMigrator(report.getReport(), appModel);

    genericGlobalEndpoint = new GenericGlobalEndpoint();
    genericGlobalEndpoint.setApplicationModel(appModel);

    imapGlobalEndpoint = new ImapGlobalEndpoint();
    imapGlobalEndpoint.setApplicationModel(appModel);
    imapsGlobalEndpoint = new ImapsGlobalEndpoint();
    imapsGlobalEndpoint.setApplicationModel(appModel);
    imapInboundEndpoint = new ImapInboundEndpoint();
    imapInboundEndpoint.setExpressionMigrator(expressionMigrator);
    imapInboundEndpoint.setApplicationModel(appModel);
    imapsInboundEndpoint = new ImapsInboundEndpoint();
    imapsInboundEndpoint.setExpressionMigrator(expressionMigrator);
    imapsInboundEndpoint.setApplicationModel(appModel);
    emailTransformers = new EmailTransformers();
    emailTransformers.setApplicationModel(appModel);
    emailConfig = new EmailConnectorConfig();
    emailConfig.setApplicationModel(appModel);
    inboundEndpoint = new InboundEndpoint();
    inboundEndpoint.setExpressionMigrator(expressionMigrator);
    inboundEndpoint.setApplicationModel(appModel);
    messageAttachmentsListExpressionEvaluator = new MessageAttachmentsListExpressionEvaluator();
    messageAttachmentsListExpressionEvaluator.setApplicationModel(appModel);
    removeSyntheticMigrationAttributes = new RemoveSyntheticMigrationAttributes();
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, genericGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> genericGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, imapGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> imapGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, imapsGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> imapsGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, imapInboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> imapInboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, imapsInboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> imapsInboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, emailTransformers.getAppliedTo().getExpression())
        .forEach(node -> emailTransformers.execute(node, report.getReport()));
    getElementsFromDocument(doc, inboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> inboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, emailConfig.getAppliedTo().getExpression())
        .forEach(node -> emailConfig.execute(node, report.getReport()));
    getElementsFromDocument(doc, messageAttachmentsListExpressionEvaluator.getAppliedTo().getExpression())
        .forEach(node -> messageAttachmentsListExpressionEvaluator.execute(node, report.getReport()));

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
