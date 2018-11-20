/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.json;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.mule.steps.validation.ValidationAllProcessorMigration;
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

@RunWith(Parameterized.class)
public class JsonModuleTest {

  private static final Path JSON_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/json");

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  @Parameterized.Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "json-01",
        "json-02",
        "json-03",
        "json-04",
        "json-05",
        "json-06",
        "json-07",
        "json-08",
        "json-09",
        "json-10",
        "json-11",
        "json-12",
        "json-13",
        "json-14",
        "json-15",
        "json-16"

    };
  }

  private final Path configPath;
  private final Path targetPath;
  private Document doc;

  public JsonModuleTest(String filePrefix) {
    configPath = JSON_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = JSON_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private ValidationAllProcessorMigration validationAllProcessorMigration;
  private JsonValidateSchema validateSchema;
  private JsonSchemaValidationFilter validateSchemaFilter;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());


    validationAllProcessorMigration = new ValidationAllProcessorMigration();
    validateSchema = new JsonValidateSchema();
    validateSchemaFilter = new JsonSchemaValidationFilter();
    // validateSchema
    // .setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), mock(ApplicationModel.class)));
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, validationAllProcessorMigration.getAppliedTo().getExpression())
        .forEach(node -> validationAllProcessorMigration.execute(node, report.getReport()));
    getElementsFromDocument(doc, validateSchema.getAppliedTo().getExpression())
        .forEach(node -> validateSchema.execute(node, report.getReport()));
    getElementsFromDocument(doc, validateSchemaFilter.getAppliedTo().getExpression())
        .forEach(node -> validateSchemaFilter.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
