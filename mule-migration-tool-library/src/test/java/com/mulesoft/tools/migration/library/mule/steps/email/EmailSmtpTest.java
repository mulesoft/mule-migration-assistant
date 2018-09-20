/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.email;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.mule.steps.core.GenericGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveSyntheticMigrationAttributes;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.CustomFilter;
import com.mulesoft.tools.migration.library.mule.steps.endpoint.InboundEndpoint;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.Element;
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
import java.util.List;

@RunWith(Parameterized.class)
public class EmailSmtpTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

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
  private final MigrationReport reportMock;

  public EmailSmtpTest(String filePrefix) {
    configPath = EMAIL_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = EMAIL_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
    reportMock = mock(MigrationReport.class);
  }

  private GenericGlobalEndpoint genericGlobalEndpoint;
  private CustomFilter customFilter;
  // private FileGlobalEndpoint smtpGlobalEndpoint;
  // private FileGlobalEndpoint smtpsGlobalEndpoint;
  private SmtpOutboundEndpoint smtpOutboundEndpoint;
  private SmtpsOutboundEndpoint smtpsOutboundEndpoint;
  // private FileTransformers emailTransformers;
  private EmailConnectorConfig emailConfig;
  private InboundEndpoint inboundEndpoint;
  private RemoveSyntheticMigrationAttributes removeSyntheticMigrationAttributes;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());

    customFilter = new CustomFilter();

    MelToDwExpressionMigrator expressionMigrator = new MelToDwExpressionMigrator(reportMock, mock(ApplicationModel.class));
    appModel = mock(ApplicationModel.class);
    when(appModel.getNodes(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc, (String) invocation.getArguments()[0]));
    when(appModel.getNode(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc, (String) invocation.getArguments()[0]).iterator().next());
    when(appModel.getNodeOptional(any(String.class)))
        .thenAnswer(invocation -> {
          List<Element> elementsFromDocument = getElementsFromDocument(doc, (String) invocation.getArguments()[0]);
          if (elementsFromDocument.isEmpty()) {
            return empty();
          } else {
            return of(elementsFromDocument.iterator().next());
          }
        });
    when(appModel.getProjectBasePath()).thenReturn(temp.newFolder().toPath());

    genericGlobalEndpoint = new GenericGlobalEndpoint();
    genericGlobalEndpoint.setApplicationModel(appModel);

    // smtpGlobalEndpoint = new FileGlobalEndpoint();
    // smtpGlobalEndpoint.setApplicationModel(appModel);
    // smtpsGlobalEndpoint = new FileGlobalEndpoint();
    // smtpsGlobalEndpoint.setApplicationModel(appModel);
    smtpOutboundEndpoint = new SmtpOutboundEndpoint();
    smtpOutboundEndpoint.setExpressionMigrator(expressionMigrator);
    smtpOutboundEndpoint.setApplicationModel(appModel);
    smtpsOutboundEndpoint = new SmtpsOutboundEndpoint();
    smtpsOutboundEndpoint.setExpressionMigrator(expressionMigrator);
    smtpsOutboundEndpoint.setApplicationModel(appModel);
    // emailTransformers = new FileTransformers();
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
        .forEach(node -> genericGlobalEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, customFilter.getAppliedTo().getExpression())
        .forEach(node -> customFilter.execute(node, mock(MigrationReport.class)));
    // getElementsFromDocument(doc, imapGlobalEndpoint.getAppliedTo().getExpression())
    // .forEach(node -> imapGlobalEndpoint.execute(node, mock(MigrationReport.class)));
    // getElementsFromDocument(doc, imapsGlobalEndpoint.getAppliedTo().getExpression())
    // .forEach(node -> imapsGlobalEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, smtpOutboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> smtpOutboundEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, smtpsOutboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> smtpsOutboundEndpoint.execute(node, mock(MigrationReport.class)));
    // getElementsFromDocument(doc, emailTransformers.getAppliedTo().getExpression())
    // .forEach(node -> emailTransformers.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, inboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> inboundEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, emailConfig.getAppliedTo().getExpression())
        .forEach(node -> emailConfig.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, removeSyntheticMigrationAttributes.getAppliedTo().getExpression())
        .forEach(node -> removeSyntheticMigrationAttributes.execute(node, mock(MigrationReport.class)));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
