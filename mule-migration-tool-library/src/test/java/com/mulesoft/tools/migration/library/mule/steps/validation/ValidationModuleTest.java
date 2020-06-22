/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.validation;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

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

@RunWith(Parameterized.class)
public class ValidationModuleTest {

  private static final Path CORE_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/core");

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  @Parameterized.Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "validation-01",
        "validation-02",
        "validation-03",
        "validation-04"

    };
  }

  private final Path configPath;
  private final Path targetPath;
  private Document doc;

  public ValidationModuleTest(String filePrefix) {
    configPath = CORE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = CORE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private ValidationMigration validationModule;
  private ValidationAllProcessorMigration validationAllProcessorMigration;
  private ValidationI18NMigration validationI18NMigration;
  private CustomValidationMigration customValidationMigration;
  private ExceptionFactoryValidationMigration exceptionFactoryValidationMigration;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());

    validationModule = new ValidationMigration();
    validationModule
        .setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), mock(ApplicationModel.class)));

    validationAllProcessorMigration = new ValidationAllProcessorMigration();
    validationI18NMigration = new ValidationI18NMigration();
    customValidationMigration = new CustomValidationMigration();
    exceptionFactoryValidationMigration = new ExceptionFactoryValidationMigration();
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, validationModule.getAppliedTo().getExpression())
        .forEach(node -> validationModule.execute(node, report.getReport()));

    getElementsFromDocument(doc, validationAllProcessorMigration.getAppliedTo().getExpression())
        .forEach(node -> validationAllProcessorMigration.execute(node, report.getReport()));

    getElementsFromDocument(doc, validationI18NMigration.getAppliedTo().getExpression())
        .forEach(node -> validationI18NMigration.execute(node, report.getReport()));

    getElementsFromDocument(doc, customValidationMigration.getAppliedTo().getExpression())
        .forEach(node -> customValidationMigration.execute(node, report.getReport()));

    getElementsFromDocument(doc, exceptionFactoryValidationMigration.getAppliedTo().getExpression())
        .forEach(node -> exceptionFactoryValidationMigration.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
