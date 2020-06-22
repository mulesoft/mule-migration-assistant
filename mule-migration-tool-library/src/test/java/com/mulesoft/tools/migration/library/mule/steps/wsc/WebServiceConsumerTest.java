/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.wsc;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.mule.steps.http.HttpConfig;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorHeaders;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorRequestConfig;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorRequester;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpOutboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpsOutboundEndpoint;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.Namespace;
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
public class WebServiceConsumerTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path WSC_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/wsc");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "wsc-config-01",
        "wsc-config-02",
        "wsc-config-03",
        "wsc-config-03b",
        "wsc-config-04",
        "wsc-config-05",
        "wsc-config-06",
        "wsc-config-07",
        "wsc-config-08",
        // TODO MMT-162
        // "wsc-config-09",
        "wsc-config-10",
        "wsc-config-11",
        "wsc-config-12",
        "wsc-config-13",
        // TODO useConnectorToRetrieveWsdl (always true)
        "wsc-config-14",
        "wsc-config-15",
        // TODO MMT-24
        // "wsc-config-16",
        "wsc-config-17",
        // TODO MMT-162
        // "wsc-config-18"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public WebServiceConsumerTest(String filePrefix) {
    configPath = WSC_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = WSC_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private HttpOutboundEndpoint httpOutbound;
  private HttpsOutboundEndpoint httpsOutbound;

  private HttpConnectorRequestConfig httpRequesterConfig;
  private HttpConnectorRequester httpRequester;
  private HttpConnectorHeaders httpHeaders;
  private WsConsumerConfig wsConsumerConfig;
  private WsConsumer wsConsumer;

  private HttpConfig httpConfig;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    doc.getRootElement().addNamespaceDeclaration(Namespace.getNamespace("wsc", "http://www.mulesoft.org/schema/mule/wsc"));
    appModel = mockApplicationModel(doc, temp);

    httpOutbound = new HttpOutboundEndpoint();
    httpOutbound.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), appModel));
    httpOutbound.setApplicationModel(appModel);

    httpsOutbound = new HttpsOutboundEndpoint();
    httpsOutbound.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), appModel));
    httpsOutbound.setApplicationModel(appModel);

    httpRequesterConfig = new HttpConnectorRequestConfig();
    httpRequesterConfig.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), appModel));

    httpRequester = new HttpConnectorRequester();
    httpRequester.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), appModel));
    httpRequester.setApplicationModel(appModel);

    httpHeaders = new HttpConnectorHeaders();
    httpHeaders.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), appModel));

    wsConsumerConfig = new WsConsumerConfig();
    wsConsumerConfig.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), appModel));
    wsConsumerConfig.setApplicationModel(appModel);

    wsConsumer = new WsConsumer();
    wsConsumer.setApplicationModel(appModel);

    httpConfig = new HttpConfig();
    httpConfig.setApplicationModel(appModel);
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, httpOutbound.getAppliedTo().getExpression())
        .forEach(node -> httpOutbound.execute(node, report.getReport()));
    getElementsFromDocument(doc, httpsOutbound.getAppliedTo().getExpression())
        .forEach(node -> httpsOutbound.execute(node, report.getReport()));

    getElementsFromDocument(doc, httpRequesterConfig.getAppliedTo().getExpression())
        .forEach(node -> httpRequesterConfig.execute(node, report.getReport()));
    getElementsFromDocument(doc, httpRequester.getAppliedTo().getExpression())
        .forEach(node -> httpRequester.execute(node, report.getReport()));
    getElementsFromDocument(doc, httpHeaders.getAppliedTo().getExpression())
        .forEach(node -> httpHeaders.execute(node, report.getReport()));
    getElementsFromDocument(doc, wsConsumerConfig.getAppliedTo().getExpression())
        .forEach(node -> wsConsumerConfig.execute(node, report.getReport()));
    getElementsFromDocument(doc, wsConsumer.getAppliedTo().getExpression())
        .forEach(node -> wsConsumer.execute(node, report.getReport()));

    getElementsFromDocument(doc, httpConfig.getAppliedTo().getExpression())
        .forEach(node -> httpConfig.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
