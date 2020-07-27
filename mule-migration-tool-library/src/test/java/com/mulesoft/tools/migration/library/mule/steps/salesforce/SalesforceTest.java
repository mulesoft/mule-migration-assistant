/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.salesforce;

import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
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
public class SalesforceTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path SALESFORCE_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/salesforce");

  @Parameterized.Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "salesforce-create",
        "salesforce-createWithoutHeaders",
        "salesforce-createWithAccessTokenId",
        "salesforce-createWithCreateObjectsManually",
        "salesforce-createWithEditInlineHeaders",
        "salesforce-upsert",
        "salesforce-upsertWithAccessTokenId",
        "salesforce-upsertWithoutHeaders",
        "salesforce-upsertWithCreateObjectsManually",
        "salesforce-upsertWithEditInlineHeaders",
        "salesforce-upsertWithoutExternalIdFieldName",
        "salesforce-retrieveWithIdsAndFieldsAddedManually",
        "salesforce-retrieveWithIdsAndFieldsFromExpression",
        "salesforce-retrieveWithIdsAddedManuallyAndFieldsFromExpression",
        "salesforce-retrieveWithEditInLineHeaders",
        "salesforce-retrieveWithAccessTokenId",
        "salesforce-retrieveWithoutIds"
    };
  }

  private final Path configPath;
  private final Path targetPath;
  private CreateOperation createOperation;
  private Document doc;
  private ApplicationModel appModel;
  private UpsertOperation upsertOperation;
  private RetrieveOperation retrieveOperation;

  public SalesforceTest(String filePrefix) {
    this.configPath = SALESFORCE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    this.targetPath = SALESFORCE_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    appModel = mockApplicationModel(doc, temp);

    createOperation = new CreateOperation();
    upsertOperation = new UpsertOperation();
    retrieveOperation = new RetrieveOperation();

    MelToDwExpressionMigrator expressionMigrator = new MelToDwExpressionMigrator(report.getReport(), appModel);
    createOperation.setExpressionMigrator(expressionMigrator);
    upsertOperation.setExpressionMigrator(expressionMigrator);
    retrieveOperation.setExpressionMigrator(expressionMigrator);
  }

  public void migrate(AbstractApplicationModelMigrationStep migrationStep) {
    getElementsFromDocument(doc, migrationStep.getAppliedTo().getExpression())
        .forEach(node -> migrationStep.execute(node, report.getReport()));
  }

  @Test
  public void execute() throws Exception {
    migrate(createOperation);
    migrate(upsertOperation);
    migrate(retrieveOperation);

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    if (doc.getBaseURI().contains("AccessTokenId")) {
      report.expectReportEntry("salesforce.accessTokenId");
    }

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }
}
