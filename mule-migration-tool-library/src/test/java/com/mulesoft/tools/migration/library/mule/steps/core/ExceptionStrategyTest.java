/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorListener;
import com.mulesoft.tools.migration.library.mule.steps.http.HttpConnectorListenerConfig;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

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

@RunWith(Parameterized.class)
public class ExceptionStrategyTest {

  private static final Path CORE_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/core");

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

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
  private ApplicationModel appModel;
  private Document doc;

  public ExceptionStrategyTest(String filePrefix) {
    configPath = CORE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = CORE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
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
    appModel = mockApplicationModel(doc, temp);

    httpListenerConfig = new HttpConnectorListenerConfig();

    httpListener = new HttpConnectorListener();
    httpListener.setApplicationModel(appModel);
    httpListener.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), appModel));

    catchExceptionStrategy = new CatchExceptionStrategy();
    catchExceptionStrategy.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), appModel));
    choiceExceptionStrategy = new ChoiceExceptionStrategy();
    exceptionStrategyRef = new ExceptionStrategyRef();
    rollbackExceptionStrategy = new RollbackExceptionStrategy();
    rollbackExceptionStrategy.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), appModel));
    removeSyntheticMigrationAttributes = new RemoveSyntheticMigrationAttributes();
  }

  @Test
  public void execute() throws Exception {
    Document doc =
        getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    getElementsFromDocument(doc, httpListenerConfig.getAppliedTo().getExpression())
        .forEach(node -> httpListenerConfig.execute(node, report.getReport()));
    getElementsFromDocument(doc, httpListener.getAppliedTo().getExpression())
        .forEach(node -> httpListener.execute(node, report.getReport()));
    getElementsFromDocument(doc, choiceExceptionStrategy.getAppliedTo().getExpression())
        .forEach(node -> choiceExceptionStrategy.execute(node, report.getReport()));
    getElementsFromDocument(doc, catchExceptionStrategy.getAppliedTo().getExpression())
        .forEach(node -> catchExceptionStrategy.execute(node, report.getReport()));
    getElementsFromDocument(doc, exceptionStrategyRef.getAppliedTo().getExpression())
        .forEach(node -> exceptionStrategyRef.execute(node, report.getReport()));
    getElementsFromDocument(doc, rollbackExceptionStrategy.getAppliedTo().getExpression())
        .forEach(node -> rollbackExceptionStrategy.execute(node, report.getReport()));
    getElementsFromDocument(doc, removeSyntheticMigrationAttributes.getAppliedTo().getExpression())
        .forEach(node -> removeSyntheticMigrationAttributes.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
