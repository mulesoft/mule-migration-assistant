/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorListener;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorListenerConfig;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
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

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

@RunWith(Parameterized.class)
public class ExceptionStrategyTest {

  private static final Path CORE_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/core");

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "error-handling-01",
        //        "error-handling-02",
        "error-handling-03",
        "error-handling-04",
        "error-handling-05"
    };
  }

  private final Path configPath;
  private final Path targetPath;
  private final MigrationReport reportMock;
  private final ApplicationModel appModel;
  private Document doc;

  public ExceptionStrategyTest(String filePrefix) {
    configPath = CORE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = CORE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
    reportMock = mock(MigrationReport.class);
    appModel = mock(ApplicationModel.class);
  }

  private CatchExceptionStrategy catchExceptionStrategy;
  private ChoiceExceptionStrategy choiceExceptionStrategy;
  private ExceptionStrategyRef exceptionStrategyRef;
  private RollbackExceptionStrategy rollbackExceptionStrategy;
  private RemoveSyntheticMigrationAttributes removeSyntheticMigrationAttributes;
  private HttpConnectorListenerConfig httpListenerConfig;
  private HttpConnectorListener httpListener;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());

    httpListenerConfig = new HttpConnectorListenerConfig();

    httpListener = new HttpConnectorListener();
    when(appModel.getNode(any(String.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc, (String) invocation.getArguments()[0]).iterator().next());
    when(appModel.getProjectBasePath()).thenReturn(temp.newFolder().toPath());
    httpListener.setApplicationModel(appModel);
    httpListener.setExpressionMigrator(new MelToDwExpressionMigrator(reportMock, appModel));

    catchExceptionStrategy = new CatchExceptionStrategy();
    catchExceptionStrategy.setExpressionMigrator(new MelToDwExpressionMigrator(reportMock, appModel));
    choiceExceptionStrategy = new ChoiceExceptionStrategy();
    exceptionStrategyRef = new ExceptionStrategyRef();
    rollbackExceptionStrategy = new RollbackExceptionStrategy();
    rollbackExceptionStrategy.setExpressionMigrator(new MelToDwExpressionMigrator(reportMock, appModel));
    removeSyntheticMigrationAttributes = new RemoveSyntheticMigrationAttributes();
  }

  @Test
  public void execute() throws Exception {
    Document doc =
        getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    getElementsFromDocument(doc, httpListenerConfig.getAppliedTo().getExpression())
        .forEach(node -> httpListenerConfig.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, httpListener.getAppliedTo().getExpression())
        .forEach(node -> httpListener.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, choiceExceptionStrategy.getAppliedTo().getExpression())
        .forEach(node -> choiceExceptionStrategy.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, catchExceptionStrategy.getAppliedTo().getExpression())
        .forEach(node -> catchExceptionStrategy.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, exceptionStrategyRef.getAppliedTo().getExpression())
        .forEach(node -> exceptionStrategyRef.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, rollbackExceptionStrategy.getAppliedTo().getExpression())
        .forEach(node -> rollbackExceptionStrategy.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, removeSyntheticMigrationAttributes.getAppliedTo().getExpression())
        .forEach(node -> removeSyntheticMigrationAttributes.execute(node, mock(MigrationReport.class)));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
