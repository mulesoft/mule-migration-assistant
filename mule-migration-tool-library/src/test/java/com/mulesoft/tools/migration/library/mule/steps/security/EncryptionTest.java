/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.security;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.tck.ReportVerification;

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

@Ignore
@RunWith(Parameterized.class)
public class EncryptionTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path ENCRYPTION_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/security/encryption");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "encryption-01",
        "encryption-02",
        "encryption-03",
        "encryption-04",
        "encryption-05",
        "encryption-06",
        "encryption-07",
        "encryption-08",
        "encryption-09",
        "encryption-10",
        "encryption-11",
        "encryption-12",
        "encryption-13",
        "encryption-14",
        "encryption-15",
        "encryption-16",
        "encryption-17",
        "encryption-18",
        "encryption-19",
        "encryption-20",
        "encryption-21",
        "encryption-22",
        "encryption-23",
        "encryption-24",
        "encryption-25",
        "encryption-26",
        "encryption-27"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public EncryptionTest(String filePrefix) {
    configPath = ENCRYPTION_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = ENCRYPTION_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    appModel = mockApplicationModel(doc, temp);

    MelToDwExpressionMigrator expressionMigrator = new MelToDwExpressionMigrator(report.getReport(), appModel);
  }

  public void migrate(AbstractApplicationModelMigrationStep migrationStep) {
    getElementsFromDocument(doc, migrationStep.getAppliedTo().getExpression())
        .forEach(node -> migrationStep.execute(node, report.getReport()));
  }

  @Test
  public void execute() throws Exception {
    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
