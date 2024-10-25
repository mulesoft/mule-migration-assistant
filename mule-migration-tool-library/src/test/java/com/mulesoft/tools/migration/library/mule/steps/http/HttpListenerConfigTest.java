/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.http;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.exception.MigrationStepException;
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
public class HttpListenerConfigTest {

  private static final Path HTTP_LISTENER_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/http");

  @Rule
  public ReportVerification report = new ReportVerification();

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "http-listener-config-01",
        "http-listener-config-02",
        "http-listener-config-03",
        "http-listener-config-04",
        "http-listener-config-05",
        "http-listener-config-06",
        "http-listener-config-07"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public HttpListenerConfigTest(String filePrefix) {
    configPath = HTTP_LISTENER_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = HTTP_LISTENER_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private HttpConnectorListenerConfig httpListenerConfig;

  @Before
  public void setUp() throws Exception {
    httpListenerConfig = new HttpConnectorListenerConfig();
  }

  @Test
  public void execute() throws Exception {
    Document doc =
        getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    getElementsFromDocument(doc, httpListenerConfig.getAppliedTo().getExpression())
        .forEach(node -> httpListenerConfig.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
