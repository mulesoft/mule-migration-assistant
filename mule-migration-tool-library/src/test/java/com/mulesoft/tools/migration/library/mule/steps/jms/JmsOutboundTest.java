/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.jms;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.mule.steps.core.GenericGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.endpoint.OutboundEndpoint;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RunWith(Parameterized.class)
public class JmsOutboundTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  private static final Path JMS_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/jms");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "jms-outbound-01",
        "jms-outbound-02",
        "jms-outbound-03",
        "jms-outbound-04",
        "jms-outbound-05",
        "jms-outbound-06",
        "jms-outbound-07",
        "jms-outbound-08",
        "jms-outbound-09",
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public JmsOutboundTest(String jmsPrefix) {
    configPath = JMS_CONFIG_EXAMPLES_PATH.resolve(jmsPrefix + "-original.xml");
    targetPath = JMS_CONFIG_EXAMPLES_PATH.resolve(jmsPrefix + ".xml");
  }

  private GenericGlobalEndpoint genericGlobalEndpoint;
  // private VmGlobalEndpoint jmsGlobalEndpoint;
  private JmsOutboundEndpoint jmsOutboundEndpoint;
  private OutboundEndpoint outboundEndpoint;
  private JmsConnector jmsConfig;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());

    appModel = mock(ApplicationModel.class);
    when(appModel.getNodes(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc, (String) invocation.getArguments()[0]));
    when(appModel.getNode(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc, (String) invocation.getArguments()[0]).iterator().next());
    when(appModel.getNodeOptional(any(String.class)))
        .thenAnswer(invocation -> {
          List<Element> elementsFromDocument = getElementsFromDocument(doc, (String) invocation.getArguments()[0]);
          if (elementsFromDocument.isEmpty()) {
            return empty();
          } else {
            return of(elementsFromDocument.iterator().next());
          }
        });
    when(appModel.getProjectBasePath()).thenReturn(temp.newFolder().toPath());
    when(appModel.getPomModel()).thenReturn(of(mock(PomModel.class)));

    MelToDwExpressionMigrator expressionMigrator =
        new MelToDwExpressionMigrator(mock(MigrationReport.class), mock(ApplicationModel.class));

    genericGlobalEndpoint = new GenericGlobalEndpoint();
    genericGlobalEndpoint.setApplicationModel(appModel);

    jmsOutboundEndpoint = new JmsOutboundEndpoint();
    jmsOutboundEndpoint.setApplicationModel(appModel);
    jmsOutboundEndpoint.setExpressionMigrator(expressionMigrator);
    outboundEndpoint = new OutboundEndpoint();
    outboundEndpoint.setApplicationModel(appModel);
    outboundEndpoint.setExpressionMigrator(expressionMigrator);
    jmsConfig = new JmsConnector();
    jmsConfig.setApplicationModel(appModel);
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, genericGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> genericGlobalEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, jmsOutboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> jmsOutboundEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, outboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> outboundEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, jmsConfig.getAppliedTo().getExpression())
        .forEach(node -> jmsConfig.execute(node, mock(MigrationReport.class)));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
