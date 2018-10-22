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
import com.mulesoft.tools.migration.tck.ReportVerification;

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
public class EmailPop3Test {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path EMAIL_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/email");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "email-pop3-inbound-01",
        "email-pop3-inbound-02",
        "email-pop3-inbound-03",
        "email-pop3-inbound-04",
        "email-pop3-inbound-05",
        "email-pop3-inbound-06",
        "email-pop3-inbound-07"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public EmailPop3Test(String filePrefix) {
    configPath = EMAIL_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = EMAIL_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private GenericGlobalEndpoint genericGlobalEndpoint;
  private CustomFilter customFilter;
  private Pop3GlobalEndpoint pop3GlobalEndpoint;
  private Pop3sGlobalEndpoint pop3sGlobalEndpoint;
  private Pop3InboundEndpoint pop3InboundEndpoint;
  private Pop3sInboundEndpoint pop3sInboundEndpoint;
  private EmailTransformers emailTransformers;
  private EmailConnectorConfig emailConfig;
  private InboundEndpoint inboundEndpoint;
  private RemoveSyntheticMigrationAttributes removeSyntheticMigrationAttributes;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());

    customFilter = new CustomFilter();

    MelToDwExpressionMigrator expressionMigrator =
        new MelToDwExpressionMigrator(report.getReport(), mock(ApplicationModel.class));
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

    pop3GlobalEndpoint = new Pop3GlobalEndpoint();
    pop3GlobalEndpoint.setApplicationModel(appModel);
    pop3sGlobalEndpoint = new Pop3sGlobalEndpoint();
    pop3sGlobalEndpoint.setApplicationModel(appModel);
    pop3InboundEndpoint = new Pop3InboundEndpoint();
    pop3InboundEndpoint.setExpressionMigrator(expressionMigrator);
    pop3InboundEndpoint.setApplicationModel(appModel);
    pop3sInboundEndpoint = new Pop3sInboundEndpoint();
    pop3sInboundEndpoint.setExpressionMigrator(expressionMigrator);
    pop3sInboundEndpoint.setApplicationModel(appModel);
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
    getElementsFromDocument(doc, pop3GlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> pop3GlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, pop3sGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> pop3sGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, pop3InboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> pop3InboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, pop3sInboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> pop3sInboundEndpoint.execute(node, report.getReport()));
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
