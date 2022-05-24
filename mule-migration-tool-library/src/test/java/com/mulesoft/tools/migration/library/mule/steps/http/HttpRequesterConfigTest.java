/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(Parameterized.class)
public class HttpRequesterConfigTest {

  private static final Path HTTP_REQUESTER_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/http");

  @Rule
  public ReportVerification report = new ReportVerification();

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "http-requester-config-01",
        "http-requester-config-02",
        "http-requester-config-03",
        "http-requester-config-04",
        "http-requester-config-05",
        "http-requester-config-06",
        "http-requester-config-07",
        "http-requester-config-08",
        "http-requester-config-09",
        "http-requester-config-10",
        "http-requester-config-11"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public HttpRequesterConfigTest(String filePrefix) {
    configPath = HTTP_REQUESTER_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = HTTP_REQUESTER_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private HttpConnectorRequestConfig httpRequesterConfig;
  private SocketsConfig socketsConfig;

  @Before
  public void setUp() throws Exception {
    httpRequesterConfig = new HttpConnectorRequestConfig();
    httpRequesterConfig
        .setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), mock(ApplicationModel.class)));
    socketsConfig = new SocketsConfig();
  }

  @Test
  public void execute() throws Exception {
    Document doc =
        getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    getElementsFromDocument(doc, httpRequesterConfig.getAppliedTo().getExpression())
        .forEach(node -> httpRequesterConfig.execute(node, report.getReport()));
    getElementsFromDocument(doc, socketsConfig.getAppliedTo().getExpression())
        .forEach(node -> socketsConfig.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
