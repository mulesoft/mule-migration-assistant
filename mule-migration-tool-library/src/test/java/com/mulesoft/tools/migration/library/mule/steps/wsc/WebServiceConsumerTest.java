/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.wsc;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.mule.steps.http.HttpConfig;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorHeaders;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorRequestConfig;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorRequester;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpOutboundEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpsOutboundEndpoint;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

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

  private static final Path WSC_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/wsc");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "wsc-config-01",
        "wsc-config-02",
        "wsc-config-03",
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
  private final MigrationReport reportMock;

  public WebServiceConsumerTest(String filePrefix) {
    configPath = WSC_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = WSC_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
    reportMock = mock(MigrationReport.class);
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
    appModel = mock(ApplicationModel.class);
    doc.getRootElement().addNamespaceDeclaration(Namespace.getNamespace("wsc", "http://www.mulesoft.org/schema/mule/wsc"));
    when(appModel.getNode(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc, (String) invocation.getArguments()[0]).iterator().next());
    when(appModel.getNodes(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc, (String) invocation.getArguments()[0]));
    when(appModel.getNodeOptional(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc, (String) invocation.getArguments()[0]).stream().findAny());
    when(appModel.getProjectBasePath()).thenReturn(temp.newFolder().toPath());

    httpOutbound = new HttpOutboundEndpoint();
    httpOutbound.setExpressionMigrator(new MelToDwExpressionMigrator(reportMock, appModel));
    httpOutbound.setApplicationModel(appModel);

    httpsOutbound = new HttpsOutboundEndpoint();
    httpsOutbound.setExpressionMigrator(new MelToDwExpressionMigrator(reportMock, appModel));
    httpsOutbound.setApplicationModel(appModel);

    httpRequesterConfig = new HttpConnectorRequestConfig();
    httpRequesterConfig.setExpressionMigrator(new MelToDwExpressionMigrator(reportMock, appModel));

    httpRequester = new HttpConnectorRequester();
    httpRequester.setExpressionMigrator(new MelToDwExpressionMigrator(reportMock, appModel));
    httpRequester.setApplicationModel(appModel);

    httpHeaders = new HttpConnectorHeaders();
    httpHeaders.setExpressionMigrator(new MelToDwExpressionMigrator(reportMock, appModel));

    wsConsumerConfig = new WsConsumerConfig();
    wsConsumerConfig.setExpressionMigrator(new MelToDwExpressionMigrator(reportMock, appModel));
    wsConsumerConfig.setApplicationModel(appModel);

    wsConsumer = new WsConsumer();
    wsConsumer.setApplicationModel(appModel);

    httpConfig = new HttpConfig();
    httpConfig.setApplicationModel(appModel);
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, httpOutbound.getAppliedTo().getExpression())
        .forEach(node -> httpOutbound.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, httpsOutbound.getAppliedTo().getExpression())
        .forEach(node -> httpsOutbound.execute(node, mock(MigrationReport.class)));

    getElementsFromDocument(doc, httpRequesterConfig.getAppliedTo().getExpression())
        .forEach(node -> httpRequesterConfig.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, httpRequester.getAppliedTo().getExpression())
        .forEach(node -> httpRequester.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, httpHeaders.getAppliedTo().getExpression())
        .forEach(node -> httpHeaders.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, wsConsumerConfig.getAppliedTo().getExpression())
        .forEach(node -> wsConsumerConfig.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, wsConsumer.getAppliedTo().getExpression())
        .forEach(node -> wsConsumer.execute(node, mock(MigrationReport.class)));

    getElementsFromDocument(doc, httpConfig.getAppliedTo().getExpression())
        .forEach(node -> httpConfig.execute(node, mock(MigrationReport.class)));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
