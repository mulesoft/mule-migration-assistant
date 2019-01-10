/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.jms;

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
public class JmsConfigTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path JMS_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/jms");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "jms-config-01",
        "jms-config-02",
        "jms-config-03",
        "jms-config-04",
        "jms-config-05",
        "jms-config-06",
        "jms-config-07",
        "jms-config-08",
        "jms-config-09",
        "jms-config-10",
        "jms-config-11",
        "jms-config-12",
        "jms-config-13",
        "jms-config-14"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public JmsConfigTest(String jmsPrefix) {
    configPath = JMS_CONFIG_EXAMPLES_PATH.resolve(jmsPrefix + "-original.xml");
    targetPath = JMS_CONFIG_EXAMPLES_PATH.resolve(jmsPrefix + ".xml");
  }

  private GenericGlobalEndpoint genericGlobalEndpoint;
  private CustomFilter customFilter;
  private JmsInboundEndpoint jmsInboundEndpoint;
  private InboundEndpoint inboundEndpoint;
  private JmsOutboundEndpoint jmsOutboundEndpoint;
  private OutboundEndpoint outboundEndpoint;
  private JmsConnector jmsConfig;
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

    jmsInboundEndpoint = new JmsInboundEndpoint();
    jmsInboundEndpoint.setExpressionMigrator(expressionMigrator);
    jmsInboundEndpoint.setApplicationModel(appModel);
    inboundEndpoint = new InboundEndpoint();
    inboundEndpoint.setExpressionMigrator(expressionMigrator);
    inboundEndpoint.setApplicationModel(appModel);
    jmsOutboundEndpoint = new JmsOutboundEndpoint();
    jmsOutboundEndpoint.setExpressionMigrator(expressionMigrator);
    jmsOutboundEndpoint.setApplicationModel(appModel);
    outboundEndpoint = new OutboundEndpoint();
    outboundEndpoint.setExpressionMigrator(expressionMigrator);
    outboundEndpoint.setApplicationModel(appModel);
    jmsConfig = new JmsConnector();
    jmsConfig.setApplicationModel(appModel);
    removeSyntheticMigrationAttributes = new RemoveSyntheticMigrationAttributes();
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, genericGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> genericGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, jmsInboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> jmsInboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, inboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> inboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, jmsOutboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> jmsOutboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, outboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> outboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, jmsConfig.getAppliedTo().getExpression())
        .forEach(node -> jmsConfig.execute(node, report.getReport()));

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
