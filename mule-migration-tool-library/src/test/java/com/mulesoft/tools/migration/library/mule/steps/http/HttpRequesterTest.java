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

import com.mulesoft.tools.migration.exception.MigrationStepException;
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

@RunWith(Parameterized.class)
public class HttpRequesterTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  private static final Path HTTP_REQUESTER_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/http");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "http-requester-01",
        "http-requester-02",
        // TODO attachments
        // "http-requester-03",
        "http-requester-04",
        "http-requester-05",
        "http-requester-06",
        "http-requester-07",
        "http-requester-08",
        "http-requester-09",
        "http-requester-10",
        "http-requester-11",
        "http-requester-12",
        "http-requester-13",
        "http-requester-14"
        //TODO MMT-172
        //"http-requester-15"
    };
  }

  private final Path configPath;
  private final Path targetPath;
  private final MigrationReport reportMock;

  public HttpRequesterTest(String filePrefix) {
    configPath = HTTP_REQUESTER_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = HTTP_REQUESTER_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
    reportMock = mock(MigrationReport.class);
  }

  private HttpConnectorRequestConfig httpRequesterConfig;
  private HttpConnectorRequester httpRequester;
  private HttpConnectorHeaders httpHeaders;
  private HttpConnectorUriParams httpUriParams;
  private HttpConnectorQueryParams httpQueryParams;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());

    httpRequesterConfig = new HttpConnectorRequestConfig();
    httpRequesterConfig.setExpressionMigrator(new MelToDwExpressionMigrator(reportMock, mock(ApplicationModel.class)));
    httpRequester = new HttpConnectorRequester();
    httpRequester.setExpressionMigrator(new MelToDwExpressionMigrator(reportMock, mock(ApplicationModel.class)));
    appModel = mock(ApplicationModel.class);
    when(appModel.getNode(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc, (String) invocation.getArguments()[0]).iterator().next());
    when(appModel.getProjectBasePath()).thenReturn(temp.newFolder().toPath());
    httpRequester.setApplicationModel(appModel);

    httpHeaders = new HttpConnectorHeaders();
    httpHeaders.setExpressionMigrator(new MelToDwExpressionMigrator(reportMock, mock(ApplicationModel.class)));
    httpUriParams = new HttpConnectorUriParams();
    httpUriParams.setExpressionMigrator(new MelToDwExpressionMigrator(reportMock, mock(ApplicationModel.class)));
    httpQueryParams = new HttpConnectorQueryParams();
    httpQueryParams.setExpressionMigrator(new MelToDwExpressionMigrator(reportMock, mock(ApplicationModel.class)));
  }

  @Ignore
  @Test(expected = MigrationStepException.class)
  public void executeWithNullElement() throws Exception {
    httpRequesterConfig.execute(null, mock(MigrationReport.class));
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, httpRequesterConfig.getAppliedTo().getExpression())
        .forEach(node -> httpRequesterConfig.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, httpRequester.getAppliedTo().getExpression())
        .forEach(node -> httpRequester.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, httpHeaders.getAppliedTo().getExpression())
        .forEach(node -> httpHeaders.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, httpUriParams.getAppliedTo().getExpression())
        .forEach(node -> httpUriParams.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, httpQueryParams.getAppliedTo().getExpression())
        .forEach(node -> httpQueryParams.execute(node, mock(MigrationReport.class)));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
