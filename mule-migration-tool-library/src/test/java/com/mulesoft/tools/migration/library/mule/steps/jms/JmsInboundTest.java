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
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveSyntheticMigrationAttributes;
import com.mulesoft.tools.migration.library.mule.steps.core.filter.CustomFilter;
import com.mulesoft.tools.migration.library.mule.steps.endpoint.InboundEndpoint;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
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
public class JmsInboundTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  private static final Path JMS_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/jms");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "jms-inbound-01",
        "jms-inbound-02",
        "jms-inbound-03",
        "jms-inbound-04",
        "jms-inbound-05",
        "jms-inbound-06",
        "jms-inbound-07",
        "jms-inbound-08",
        "jms-inbound-09",
        "jms-inbound-10",
        "jms-inbound-11",
        "jms-inbound-12",
        "jms-inbound-13",
    };
  }

  private final Path configPath;
  private final Path targetPath;
  private final MigrationReport reportMock;

  public JmsInboundTest(String jmsPrefix) {
    configPath = JMS_CONFIG_EXAMPLES_PATH.resolve(jmsPrefix + "-original.xml");
    targetPath = JMS_CONFIG_EXAMPLES_PATH.resolve(jmsPrefix + ".xml");
    reportMock = mock(MigrationReport.class);
  }

  private GenericGlobalEndpoint genericGlobalEndpoint;
  private CustomFilter customFilter;
  // private VmGlobalEndpoint jmsGlobalEndpoint;
  private JmsInboundEndpoint jmsInboundEndpoint;
  private InboundEndpoint inboundEndpoint;
  private JmsConnector jmsConfig;
  private RemoveSyntheticMigrationAttributes removeSyntheticMigrationAttributes;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());

    customFilter = new CustomFilter();

    MelToDwExpressionMigrator expressionMigrator = new MelToDwExpressionMigrator(reportMock, mock(ApplicationModel.class));
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

    genericGlobalEndpoint = new GenericGlobalEndpoint();
    genericGlobalEndpoint.setApplicationModel(appModel);

    // vmGlobalEndpoint = new VmGlobalEndpoint();
    // vmGlobalEndpoint.setApplicationModel(appModel);
    jmsInboundEndpoint = new JmsInboundEndpoint();
    jmsInboundEndpoint.setExpressionMigrator(expressionMigrator);
    jmsInboundEndpoint.setApplicationModel(appModel);
    inboundEndpoint = new InboundEndpoint();
    inboundEndpoint.setExpressionMigrator(expressionMigrator);
    inboundEndpoint.setApplicationModel(appModel);
    jmsConfig = new JmsConnector();
    jmsConfig.setApplicationModel(appModel);
    removeSyntheticMigrationAttributes = new RemoveSyntheticMigrationAttributes();
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, genericGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> genericGlobalEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, customFilter.getAppliedTo().getExpression())
        .forEach(node -> customFilter.execute(node, mock(MigrationReport.class)));
    // getElementsFromDocument(doc, vmGlobalEndpoint.getAppliedTo().getExpression())
    // .forEach(node -> vmGlobalEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, jmsInboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> jmsInboundEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, inboundEndpoint.getAppliedTo().getExpression())
        .forEach(node -> inboundEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, jmsConfig.getAppliedTo().getExpression())
        .forEach(node -> jmsConfig.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, removeSyntheticMigrationAttributes.getAppliedTo().getExpression())
        .forEach(node -> removeSyntheticMigrationAttributes.execute(node, mock(MigrationReport.class)));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
