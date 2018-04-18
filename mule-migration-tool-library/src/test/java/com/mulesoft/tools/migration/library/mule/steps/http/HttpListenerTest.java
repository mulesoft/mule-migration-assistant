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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.exception.MigrationStepException;
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
public class HttpListenerTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  private static final Path HTTP_LISTENER_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/http");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "http-listener-01",
        // TODO Attachments/multipart support
        // "http-listener-02",
        // "http-listener-03",
        "http-listener-04",
        "http-listener-05",
        "http-listener-09",
        "http-listener-10",
        "http-listener-11",
        // TODO Multiheader support
        // "http-listener-12",
        // TODO MEL 2 DW
        "http-listener-13",
        "http-listener-14",
        // TODO Multiheader support
        // "http-listener-15",
        "http-listener-16",
        "http-listener-17",
        "http-listener-18",
        // TODO MEL 2 DW: map expressions
        "http-listener-19",
        "http-listener-20"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public HttpListenerTest(String filePrefix) {
    configPath = HTTP_LISTENER_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = HTTP_LISTENER_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private HttpConnectorListenerConfig httpListenerConfig;
  private HttpConnectorListener httpListener;
  private HttpConnectorHeaders httpHeaders;

  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    httpListenerConfig = new HttpConnectorListenerConfig();

    httpListener = new HttpConnectorListener();
    appModel = mock(ApplicationModel.class);
    when(appModel.getProjectBasePath()).thenReturn(temp.newFolder().toPath());
    httpListener.setApplicationModel(appModel);

    httpHeaders = new HttpConnectorHeaders();
  }

  @Ignore
  @Test(expected = MigrationStepException.class)
  public void executeWithNullElement() throws Exception {
    httpListenerConfig.execute(null, mock(MigrationReport.class));
  }

  @Test
  public void execute() throws Exception {
    Document doc =
        getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    getElementsFromDocument(doc, httpListenerConfig.getAppliedTo().getExpression())
        .forEach(node -> httpListenerConfig.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, httpListener.getAppliedTo().getExpression())
        .forEach(node -> httpListener.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, httpHeaders.getAppliedTo().getExpression())
        .forEach(node -> httpHeaders.execute(node, mock(MigrationReport.class)));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
