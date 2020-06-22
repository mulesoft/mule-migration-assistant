/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.amqp;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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

import com.mulesoft.tools.migration.library.mule.steps.core.GenericGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.endpoint.OutboundEndpoint;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

@RunWith(Parameterized.class)
public class AmqpOutboundTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path AMQP_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/amqp");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "amqp-outbound-01",
        "amqp-outbound-02",
        "amqp-outbound-03",
        "amqp-outbound-04",
        "amqp-outbound-05"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public AmqpOutboundTest(String amqpPrefix) {
    configPath = AMQP_CONFIG_EXAMPLES_PATH.resolve(amqpPrefix + "-original.xml");
    targetPath = AMQP_CONFIG_EXAMPLES_PATH.resolve(amqpPrefix + ".xml");
  }

  private GenericGlobalEndpoint genericGlobalEndpoint;
  private AmqpOutboundEndpoint amqpOutboundEndpoint;
  private OutboundEndpoint outboundEndpoint;
  private AmqpConnector amqpConfig;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());

    appModel = mockApplicationModel(doc, temp);

    MelToDwExpressionMigrator expressionMigrator =
        new MelToDwExpressionMigrator(report.getReport(), mock(ApplicationModel.class));

    genericGlobalEndpoint = new GenericGlobalEndpoint();
    genericGlobalEndpoint.setApplicationModel(appModel);

    amqpOutboundEndpoint = new AmqpOutboundEndpoint();
    amqpOutboundEndpoint.setApplicationModel(appModel);
    amqpOutboundEndpoint.setExpressionMigrator(expressionMigrator);
    outboundEndpoint = new OutboundEndpoint();
    outboundEndpoint.setApplicationModel(appModel);
    outboundEndpoint.setExpressionMigrator(expressionMigrator);
    amqpConfig = new AmqpConnector();
    amqpConfig.setApplicationModel(appModel);
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, genericGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> genericGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, amqpOutboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> amqpOutboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, outboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> outboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, amqpConfig.getAppliedTo().getExpression())
        .forEach(node -> amqpConfig.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
