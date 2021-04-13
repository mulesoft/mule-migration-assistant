/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.amqp;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.mule.steps.core.RemoveSyntheticMigrationAttributes;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class AmqpConfigTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path AMQP_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/amqp");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {"amqp-config-01", "amqp-config-02", "amqp-config-03", "amqp-config-04", "amqp-config-05",
        "amqp-config-06", "amqp-config-07", "amqp-config-08", "amqp-config-09", "amqp-config-10", "amqp-config-11",
        "amqp-config-12", "amqp-config-13"};
  }

  private final Path configPath;
  private final Path targetPath;

  public AmqpConfigTest(String jmsPrefix) {
    configPath = AMQP_CONFIG_EXAMPLES_PATH.resolve(jmsPrefix + "-original.xml");
    targetPath = AMQP_CONFIG_EXAMPLES_PATH.resolve(jmsPrefix + ".xml");
  }

  private AmqpConnector amqpConfig;
  private AmqpInboundEndpoint amqpInboundEndpoint;
  private RemoveSyntheticMigrationAttributes removeSyntheticMigrationAttributes;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());

    MelToDwExpressionMigrator expressionMigrator = new MelToDwExpressionMigrator(report.getReport(),
                                                                                 mock(ApplicationModel.class));
    appModel = mock(ApplicationModel.class);
    when(appModel.getNodes(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc, (String) invocation.getArguments()[0]));
    when(appModel.getNode(any(String.class))).thenAnswer(
                                                         invocation -> getElementsFromDocument(doc,
                                                                                               (String) invocation
                                                                                                   .getArguments()[0]).iterator()
                                                                                                       .next());
    when(appModel.getNodeOptional(any(String.class))).thenAnswer(invocation -> {
      List<Element> elementsFromDocument = getElementsFromDocument(doc, (String) invocation.getArguments()[0]);
      if (elementsFromDocument.isEmpty()) {
        return empty();
      } else {
        return of(elementsFromDocument.iterator().next());
      }
    });
    when(appModel.getProjectBasePath()).thenReturn(temp.newFolder().toPath());
    when(appModel.getPomModel()).thenReturn(of(mock(PomModel.class)));

    amqpInboundEndpoint = new AmqpInboundEndpoint();
    amqpInboundEndpoint.setExpressionMigrator(expressionMigrator);
    amqpInboundEndpoint.setApplicationModel(appModel);

    amqpConfig = new AmqpConnector();
    amqpConfig.setApplicationModel(appModel);
    removeSyntheticMigrationAttributes = new RemoveSyntheticMigrationAttributes();
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, amqpInboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> amqpInboundEndpoint.execute(node, report.getReport()));
    getElementsFromDocument(doc, removeSyntheticMigrationAttributes.getAppliedTo().getExpression())
        .forEach(node -> removeSyntheticMigrationAttributes.execute(node, report.getReport()));
    getElementsFromDocument(doc, amqpConfig.getAppliedTo().getExpression())
        .forEach(node -> amqpConfig.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils
                   .toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                       .ignoreComments().normalizeWhitespace());
  }

}
