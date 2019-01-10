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

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

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

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path HTTP_REQUESTER_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/http");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "http-requester-01",
        "http-requester-02",
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

  public HttpRequesterTest(String filePrefix) {
    configPath = HTTP_REQUESTER_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = HTTP_REQUESTER_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
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
    appModel = mockApplicationModel(doc, temp);

    httpRequesterConfig = new HttpConnectorRequestConfig();
    httpRequesterConfig.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), appModel));
    httpRequester = new HttpConnectorRequester();
    httpRequester.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), appModel));
    httpRequester.setApplicationModel(appModel);

    httpHeaders = new HttpConnectorHeaders();
    httpHeaders.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), appModel));
    httpUriParams = new HttpConnectorUriParams();
    httpUriParams.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), appModel));
    httpQueryParams = new HttpConnectorQueryParams();
    httpQueryParams.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), appModel));
  }

  @Ignore
  @Test(expected = MigrationStepException.class)
  public void executeWithNullElement() throws Exception {
    httpRequesterConfig.execute(null, report.getReport());
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, httpRequesterConfig.getAppliedTo().getExpression())
        .forEach(node -> httpRequesterConfig.execute(node, report.getReport()));
    getElementsFromDocument(doc, httpRequester.getAppliedTo().getExpression())
        .forEach(node -> httpRequester.execute(node, report.getReport()));
    getElementsFromDocument(doc, httpHeaders.getAppliedTo().getExpression())
        .forEach(node -> httpHeaders.execute(node, report.getReport()));
    getElementsFromDocument(doc, httpUriParams.getAppliedTo().getExpression())
        .forEach(node -> httpUriParams.execute(node, report.getReport()));
    getElementsFromDocument(doc, httpQueryParams.getAppliedTo().getExpression())
        .forEach(node -> httpQueryParams.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
