/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.expression;

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

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

@RunWith(Parameterized.class)
public class SimpleExpressionTransformerTest {

  private static final Path JSON_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/expression");

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private final Path configPath;
  private final Path targetPath;

  private Document doc;
  private ApplicationModel appModel;
  private SimpleExpressionTransformer simpleExpressionTransformer;

  @Parameterized.Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "simple-expression",
        "simple-expression-with-mimetype",
        "simple-expression-with-return-argument",
        "simple-expression-with-encoding",
        "simple-expression-without-mimetype-with-encoding"
    };
  }

  public SimpleExpressionTransformerTest(String filePrefix) {
    configPath = JSON_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = JSON_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    appModel = mockApplicationModel(doc, temp);
    MelToDwExpressionMigrator expressionMigrator = new MelToDwExpressionMigrator(report.getReport(), appModel);

    simpleExpressionTransformer = new SimpleExpressionTransformer();
    simpleExpressionTransformer.setExpressionMigrator(expressionMigrator);
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, simpleExpressionTransformer.getAppliedTo().getExpression())
        .forEach(node -> simpleExpressionTransformer.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
