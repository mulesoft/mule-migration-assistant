/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.db;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.tck.MockApplicationModelSupplier.mockApplicationModel;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.library.mule.steps.core.TransactionalScope;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

import java.nio.file.Path;
import java.nio.file.Paths;

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

@RunWith(Parameterized.class)
public class DbStoredProcedureTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path DB_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/db");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "db-stored-procedure-01",
        "db-stored-procedure-02",
        "db-stored-procedure-03",
        "db-stored-procedure-04",
        "db-stored-procedure-05",
        "db-stored-procedure-06",
        "db-stored-procedure-07",
        "db-stored-procedure-08",
        "db-stored-procedure-09",
        "db-stored-procedure-10"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public DbStoredProcedureTest(String filePrefix) {
    configPath = DB_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = DB_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private TransactionalScope txScope;
  private DbStoredProcedure dbStoredProcedure;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    appModel = mockApplicationModel(doc, temp);

    txScope = new TransactionalScope();
    dbStoredProcedure = new DbStoredProcedure();
    dbStoredProcedure.setApplicationModel(appModel);
    dbStoredProcedure.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), appModel));
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, txScope.getAppliedTo().getExpression())
        .forEach(node -> txScope.execute(node, report.getReport()));
    getElementsFromDocument(doc, dbStoredProcedure.getAppliedTo().getExpression())
        .forEach(node -> dbStoredProcedure.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
