/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.amqp;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.mule.steps.core.GenericGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveSyntheticMigrationAttributes;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.CustomFilter;
import com.mulesoft.tools.migration.library.mule.steps.endpoint.InboundEndpoint;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

import java.nio.file.Path;
import java.nio.file.Paths;

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

@RunWith(Parameterized.class)
public class AmqpInboundTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path AMQP_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/amqp");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "amqp-inbound-01",
        "amqp-inbound-02",
        "amqp-inbound-03",
        "amqp-inbound-04",
        "amqp-inbound-05",
        "amqp-inbound-06",
        "amqp-inbound-07",
        "amqp-inbound-08"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public AmqpInboundTest(String amqpPrefix) {
    configPath = AMQP_CONFIG_EXAMPLES_PATH.resolve(amqpPrefix + "-original.xml");
    targetPath = AMQP_CONFIG_EXAMPLES_PATH.resolve(amqpPrefix + ".xml");
  }

  private GenericGlobalEndpoint genericGlobalEndpoint;
  private CustomFilter customFilter;
  private AmqpInboundEndpoint amqpInboundEndpoint;
  private InboundEndpoint inboundEndpoint;
  private AmqpConnector amqpConfig;
  private RemoveSyntheticMigrationAttributes removeSyntheticMigrationAttributes;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());

    customFilter = new CustomFilter();

    MelToDwExpressionMigrator expressionMigrator =
        new MelToDwExpressionMigrator(report.getReport(), mock(ApplicationModel.class));
    appModel = mockApplicationModel(doc, temp);

    genericGlobalEndpoint = new GenericGlobalEndpoint();
    genericGlobalEndpoint.setApplicationModel(appModel);

    amqpInboundEndpoint = new AmqpInboundEndpoint();
    amqpInboundEndpoint.setExpressionMigrator(expressionMigrator);
    amqpInboundEndpoint.setApplicationModel(appModel);
    inboundEndpoint = new InboundEndpoint();
    inboundEndpoint.setExpressionMigrator(expressionMigrator);
    inboundEndpoint.setApplicationModel(appModel);
    amqpConfig = new AmqpConnector();
    amqpConfig.setApplicationModel(appModel);
    removeSyntheticMigrationAttributes = new RemoveSyntheticMigrationAttributes();
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, genericGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> genericGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, amqpInboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> amqpInboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, inboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> inboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, amqpConfig.getAppliedTo().getExpression())
        .forEach(node -> amqpConfig.execute(node, report.getReport()));

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
