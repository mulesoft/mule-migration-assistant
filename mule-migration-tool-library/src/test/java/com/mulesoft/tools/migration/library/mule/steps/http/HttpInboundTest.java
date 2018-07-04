/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.mule.steps.core.GenericGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveSyntheticMigrationAttributes;
import com.mulesoft.tools.migration.library.mule.steps.endpoint.InboundEndpoint;
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
public class HttpInboundTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  private static final Path HTTP_REQUESTER_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/http");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "http-inbound-01",
        "http-inbound-02",
        "http-inbound-03",
        "http-inbound-04",
        "http-inbound-05",
        "http-inbound-06",
        "http-inbound-07",
        "http-inbound-08",
        "http-inbound-09",
        "http-inbound-10",
        "http-inbound-11",
        "http-inbound-12",
        "http-inbound-13",
        "http-inbound-14",
        "http-inbound-15",
        "http-inbound-16",
        "http-inbound-17",
        "http-inbound-18",
        "http-inbound-19",
        "http-inbound-20",
        "http-inbound-21",
        "http-inbound-22",
        "http-inbound-22b",
        "http-inbound-23",
        "http-inbound-24",
        "http-inbound-25",
        "http-inbound-26",
        "http-inbound-27",
        "http-inbound-28",
        "http-inbound-29",
        "http-inbound-30",
        "http-inbound-31",
        "http-inbound-32",
        "http-inbound-33",
        "http-inbound-34",
        "http-inbound-35",
        "http-inbound-36",
        "http-inbound-37"
    };
  }

  private final Path configPath;
  private final Path targetPath;
  private final MigrationReport reportMock;

  public HttpInboundTest(String filePrefix) {
    configPath = HTTP_REQUESTER_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = HTTP_REQUESTER_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
    reportMock = mock(MigrationReport.class);
  }

  private GenericGlobalEndpoint genericGlobalEndpoint;
  private HttpPollingConnector httpPollingConnector;
  private HttpsPollingConnector httpsPollingConnector;
  private HttpGlobalEndpoint httpGlobalEndpoint;
  private HttpsGlobalEndpoint httpsGlobalEndpoint;
  private HttpInboundEndpoint httpInbound;
  private HttpsInboundEndpoint httpsInbound;
  private HttpConfig httpConfig;
  private HttpTransformers httpTransformers;
  private HttpConnectorHeaders httpHeaders;
  private HttpStaticResource httpStaticResource;
  private InboundEndpoint inboundEndpoint;
  private HttpGlobalBuilders httpGlobalBuilders;
  private RemoveSyntheticMigrationAttributes removeSyntheticMigrationAttributes;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    doc.getRootElement().addNamespaceDeclaration(Namespace.getNamespace("http", "http://www.mulesoft.org/schema/mule/http"));
    appModel = mock(ApplicationModel.class);
    when(appModel.getNodes(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc, (String) invocation.getArguments()[0]));
    when(appModel.getNode(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc, (String) invocation.getArguments()[0]).iterator().next());
    when(appModel.getNodeOptional(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc, (String) invocation.getArguments()[0]).stream().findAny());
    when(appModel.getProjectBasePath()).thenReturn(temp.newFolder().toPath());
    when(appModel.getPomModel()).thenReturn(empty());

    MelToDwExpressionMigrator expressionMigrator = new MelToDwExpressionMigrator(reportMock, mock(ApplicationModel.class));

    genericGlobalEndpoint = new GenericGlobalEndpoint();
    genericGlobalEndpoint.setApplicationModel(appModel);

    httpPollingConnector = new HttpPollingConnector();
    httpPollingConnector.setApplicationModel(appModel);
    httpsPollingConnector = new HttpsPollingConnector();
    httpsPollingConnector.setApplicationModel(appModel);

    httpGlobalEndpoint = new HttpGlobalEndpoint();
    httpGlobalEndpoint.setApplicationModel(appModel);
    httpsGlobalEndpoint = new HttpsGlobalEndpoint();
    httpsGlobalEndpoint.setApplicationModel(appModel);

    httpInbound = new HttpInboundEndpoint();
    httpInbound.setExpressionMigrator(expressionMigrator);
    httpInbound.setApplicationModel(appModel);

    httpsInbound = new HttpsInboundEndpoint();
    httpsInbound.setExpressionMigrator(expressionMigrator);
    httpsInbound.setApplicationModel(appModel);

    httpConfig = new HttpConfig();
    httpConfig.setApplicationModel(appModel);

    httpTransformers = new HttpTransformers();
    httpTransformers.setApplicationModel(appModel);

    httpHeaders = new HttpConnectorHeaders();
    httpHeaders.setExpressionMigrator(expressionMigrator);

    httpStaticResource = new HttpStaticResource();
    httpStaticResource.setExpressionMigrator(expressionMigrator);

    inboundEndpoint = new InboundEndpoint();
    inboundEndpoint.setExpressionMigrator(expressionMigrator);
    inboundEndpoint.setApplicationModel(appModel);

    httpGlobalBuilders = new HttpGlobalBuilders();
    removeSyntheticMigrationAttributes = new RemoveSyntheticMigrationAttributes();
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, genericGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> genericGlobalEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, httpPollingConnector.getAppliedTo().getExpression())
        .forEach(node -> httpPollingConnector.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, httpsPollingConnector.getAppliedTo().getExpression())
        .forEach(node -> httpsPollingConnector.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, httpGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> httpGlobalEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, httpsGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> httpsGlobalEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, httpInbound.getAppliedTo().getExpression())
        .forEach(node -> httpInbound.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, httpsInbound.getAppliedTo().getExpression())
        .forEach(node -> httpsInbound.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, httpConfig.getAppliedTo().getExpression())
        .forEach(node -> httpConfig.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, httpTransformers.getAppliedTo().getExpression())
        .forEach(node -> httpTransformers.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, httpHeaders.getAppliedTo().getExpression())
        .forEach(node -> httpHeaders.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, httpStaticResource.getAppliedTo().getExpression())
        .forEach(node -> httpStaticResource.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, inboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> inboundEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, httpGlobalBuilders.getAppliedTo().getExpression())
        .forEach(node -> httpGlobalBuilders.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, removeSyntheticMigrationAttributes.getAppliedTo().getExpression())
        .forEach(node -> removeSyntheticMigrationAttributes.execute(node, mock(MigrationReport.class)));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
