/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.email;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(Parameterized.class)
public class EmailSmtpTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path EMAIL_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/email");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "email-smtp-outbound-01",
        "email-smtp-outbound-02",
        "email-smtp-outbound-03",
        "email-smtp-outbound-04",
        "email-smtp-outbound-05",
        "email-smtp-outbound-06",
        "email-smtp-outbound-07",
        "email-smtp-outbound-08",
        "email-smtp-outbound-09"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public EmailSmtpTest(String filePrefix) {
    configPath = EMAIL_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = EMAIL_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private GenericGlobalEndpoint genericGlobalEndpoint;
  private CustomFilter customFilter;
  private SmtpGlobalEndpoint smtpGlobalEndpoint;
  private SmtpsGlobalEndpoint smtpsGlobalEndpoint;
  private SmtpOutboundEndpoint smtpOutboundEndpoint;
  private SmtpsOutboundEndpoint smtpsOutboundEndpoint;
  private EmailTransformers emailTransformers;
  private EmailConnectorConfig emailConfig;
  private InboundEndpoint inboundEndpoint;
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

    smtpGlobalEndpoint = new SmtpGlobalEndpoint();
    smtpGlobalEndpoint.setApplicationModel(appModel);
    smtpsGlobalEndpoint = new SmtpsGlobalEndpoint();
    smtpsGlobalEndpoint.setApplicationModel(appModel);
    smtpOutboundEndpoint = new SmtpOutboundEndpoint();
    smtpOutboundEndpoint.setExpressionMigrator(expressionMigrator);
    smtpOutboundEndpoint.setApplicationModel(appModel);
    smtpsOutboundEndpoint = new SmtpsOutboundEndpoint();
    smtpsOutboundEndpoint.setExpressionMigrator(expressionMigrator);
    smtpsOutboundEndpoint.setApplicationModel(appModel);
    emailTransformers = new EmailTransformers();
    emailTransformers.setApplicationModel(appModel);
    emailConfig = new EmailConnectorConfig();
    emailConfig.setApplicationModel(appModel);
    inboundEndpoint = new InboundEndpoint();
    inboundEndpoint.setExpressionMigrator(expressionMigrator);
    inboundEndpoint.setApplicationModel(appModel);
    removeSyntheticMigrationAttributes = new RemoveSyntheticMigrationAttributes();
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, genericGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> genericGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, smtpGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> smtpGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, smtpsGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> smtpsGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, smtpOutboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> smtpOutboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, smtpsOutboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> smtpsOutboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, emailTransformers.getAppliedTo().getExpression())
        .forEach(node -> emailTransformers.execute(node, report.getReport()));
    getElementsFromDocument(doc, inboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> inboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, emailConfig.getAppliedTo().getExpression())
        .forEach(node -> emailConfig.execute(node, report.getReport()));

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
