/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.requestReply;

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

import com.mulesoft.tools.migration.library.mule.steps.core.GenericGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.core.RemoveSyntheticMigrationAttributes;
import com.mulesoft.tools.migration.library.mule.steps.jms.JmsConnector;
import com.mulesoft.tools.migration.library.mule.steps.jms.JmsGlobalEndpoint;
import com.mulesoft.tools.migration.library.mule.steps.vm.VmConnector;
import com.mulesoft.tools.migration.library.mule.steps.vm.VmGlobalEndpoint;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.project.model.pom.PomModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

@RunWith(Parameterized.class)
public class RequestReplyTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  private static final Path RR_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/requestReply");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "request-reply-01",
        "request-reply-02",
        "request-reply-03",
        "request-reply-04",
        "request-reply-05",
        "request-reply-06",
        "request-reply-07",
        "request-reply-08",
        "request-reply-09",
        //        "request-reply-10"
    };
  }

  private final Path configPath;
  private final Path targetPath;
  private final MigrationReport reportMock;

  public RequestReplyTest(String jmsPrefix) {
    configPath = RR_CONFIG_EXAMPLES_PATH.resolve(jmsPrefix + "-original.xml");
    targetPath = RR_CONFIG_EXAMPLES_PATH.resolve(jmsPrefix + ".xml");
    reportMock = mock(MigrationReport.class);
  }

  private GenericGlobalEndpoint genericGlobalEndpoint;
  private RequestReply requestReply;
  private JmsGlobalEndpoint jmsGlobalEndpoint;
  private JmsConnector jmsConfig;
  private VmGlobalEndpoint vmGlobalEndpoint;
  private VmConnector vmConfig;
  private RemoveSyntheticMigrationAttributes removeSyntheticMigrationAttributes;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());

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
    when(appModel.getPomModel()).thenReturn(of(mock(PomModel.class)));

    genericGlobalEndpoint = new GenericGlobalEndpoint();
    genericGlobalEndpoint.setApplicationModel(appModel);

    requestReply = new RequestReply();
    requestReply.setApplicationModel(appModel);
    jmsGlobalEndpoint = new JmsGlobalEndpoint();
    jmsGlobalEndpoint.setApplicationModel(appModel);
    jmsConfig = new JmsConnector();
    jmsConfig.setApplicationModel(appModel);
    vmGlobalEndpoint = new VmGlobalEndpoint();
    vmGlobalEndpoint.setApplicationModel(appModel);
    vmConfig = new VmConnector();
    vmConfig.setApplicationModel(appModel);
    removeSyntheticMigrationAttributes = new RemoveSyntheticMigrationAttributes();
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, genericGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> genericGlobalEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, requestReply.getAppliedTo().getExpression())
        .forEach(node -> requestReply.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, jmsGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> jmsGlobalEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, jmsConfig.getAppliedTo().getExpression())
        .forEach(node -> jmsConfig.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, vmGlobalEndpoint.getAppliedTo().getExpression())
        .forEach(node -> vmGlobalEndpoint.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, vmConfig.getAppliedTo().getExpression())
        .forEach(node -> vmConfig.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, removeSyntheticMigrationAttributes.getAppliedTo().getExpression())
        .forEach(node -> removeSyntheticMigrationAttributes.execute(node, mock(MigrationReport.class)));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
