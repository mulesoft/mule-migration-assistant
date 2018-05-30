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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;

@Ignore
@RunWith(Parameterized.class)
public class HttpOutboundTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

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
        "http-outbound-13",
        "http-outbound-14",
        "http-outbound-15",
        "http-outbound-16",
        "http-outbound-17",
        "http-outbound-18",
        "http-outbound-19",
        "http-outbound-20"
    };
  }

  private final Path configPath;
  private final Path targetPath;
  private final MigrationReport reportMock;

  public HttpOutboundTest(String filePrefix) {
    configPath = HTTP_REQUESTER_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = HTTP_REQUESTER_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
    reportMock = mock(MigrationReport.class);
  }

  private HttpGlobalEndpoint httpGlobalEndpoint;
  private HttpsGlobalEndpoint httpsGlobalEndpoint;
  private HttpOutboundEndpoint httpOutbound;
  private HttpsOutboundEndpoint httpsOutbound;
  private HttpConfig httpConfig;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());

    appModel = mock(ApplicationModel.class);
    when(appModel.getNodes(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc, (String) invocation.getArguments()[0]));
    when(appModel.getProjectBasePath()).thenReturn(temp.newFolder().toPath());

    httpGlobalEndpoint = new HttpGlobalEndpoint();
    httpGlobalEndpoint.setApplicationModel(appModel);
    httpsGlobalEndpoint = new HttpsGlobalEndpoint();
    httpsGlobalEndpoint.setApplicationModel(appModel);

    httpOutbound = new HttpOutboundEndpoint();
    httpOutbound.setExpressionMigrator(new MelToDwExpressionMigrator(reportMock));
    httpOutbound.setApplicationModel(appModel);

    httpsOutbound = new HttpsOutboundEndpoint();
    httpsOutbound.setExpressionMigrator(new MelToDwExpressionMigrator(reportMock));
    httpsOutbound.setApplicationModel(appModel);

    httpConfig = new HttpConfig();
    httpConfig.setApplicationModel(appModel);
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, httpGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> httpGlobalEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, httpsGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> httpsGlobalEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, httpOutbound.getAppliedTo().getExpression())
        .forEach(node -> httpOutbound.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, httpsOutbound.getAppliedTo().getExpression())
        .forEach(node -> httpsOutbound.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, httpConfig.getAppliedTo().getExpression())
        .forEach(node -> httpConfig.execute(node, mock(MigrationReport.class)));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
