/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.mule.steps.core.GenericGlobalEndpoint;
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
public class HttpOutboundTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path HTTP_REQUESTER_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/http");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "http-outbound-01",
        "http-outbound-02",
        "http-outbound-03",
        "http-outbound-04",
        "http-outbound-05",
        "http-outbound-06",
        "http-outbound-07",
        "http-outbound-08",
        "http-outbound-09",
        "http-outbound-10",
        "http-outbound-11",
        "http-outbound-12",
        // TODO MMT-154 Migrate spring beans
        // "http-outbound-13",
        // TODO rest-service-component
        // "http-outbound-14",
        // "http-outbound-15",
        // "http-outbound-16",
        // "http-outbound-17",
        "http-outbound-18",
        "http-outbound-19",
        "http-outbound-20",
        "http-outbound-21",
        "http-outbound-22",
        "http-outbound-23",
        "http-outbound-24",
        "http-outbound-25",
        "http-outbound-26",
        "http-outbound-policy-01"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public HttpOutboundTest(String filePrefix) {
    configPath = HTTP_REQUESTER_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = HTTP_REQUESTER_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private GenericGlobalEndpoint genericGlobalEndpoint;
  private HttpGlobalEndpoint httpGlobalEndpoint;
  private HttpsGlobalEndpoint httpsGlobalEndpoint;
  private HttpOutboundEndpoint httpOutbound;
  private HttpsOutboundEndpoint httpsOutbound;
  private HttpConfig httpConfig;
  private HttpConnectorHeaders httpHeaders;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    doc.getRootElement().addNamespaceDeclaration(Namespace.getNamespace("http", "http://www.mulesoft.org/schema/mule/http"));
    appModel = mockApplicationModel(doc, temp);

    MelToDwExpressionMigrator expressionMigrator = new MelToDwExpressionMigrator(report.getReport(), appModel);

    genericGlobalEndpoint = new GenericGlobalEndpoint();
    genericGlobalEndpoint.setApplicationModel(appModel);

    httpGlobalEndpoint = new HttpGlobalEndpoint();
    httpGlobalEndpoint.setApplicationModel(appModel);
    httpsGlobalEndpoint = new HttpsGlobalEndpoint();
    httpsGlobalEndpoint.setApplicationModel(appModel);

    httpOutbound = new HttpOutboundEndpoint();
    httpOutbound.setExpressionMigrator(expressionMigrator);
    httpOutbound.setApplicationModel(appModel);

    httpsOutbound = new HttpsOutboundEndpoint();
    httpsOutbound.setExpressionMigrator(expressionMigrator);
    httpsOutbound.setApplicationModel(appModel);

    httpConfig = new HttpConfig();
    httpConfig.setApplicationModel(appModel);

    httpHeaders = new HttpConnectorHeaders();
    httpHeaders.setExpressionMigrator(expressionMigrator);
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, genericGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> genericGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, httpGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> httpGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, httpsGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> httpsGlobalEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, httpOutbound.getAppliedTo().getExpression())
        .forEach(node -> httpOutbound.execute(node, report.getReport()));
    getElementsFromDocument(doc, httpsOutbound.getAppliedTo().getExpression())
        .forEach(node -> httpsOutbound.execute(node, report.getReport()));
    getElementsFromDocument(doc, httpConfig.getAppliedTo().getExpression())
        .forEach(node -> httpConfig.execute(node, report.getReport()));
    getElementsFromDocument(doc, httpHeaders.getAppliedTo().getExpression())
        .forEach(node -> httpHeaders.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
