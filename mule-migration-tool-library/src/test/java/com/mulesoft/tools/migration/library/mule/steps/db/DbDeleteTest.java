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
public class DbDeleteTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path DB_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/db");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "db-delete-01",
        "db-delete-02",
        "db-delete-03",
        "db-delete-04",
        "db-delete-05",
        "db-delete-06",
        "db-delete-07",
        "db-delete-08",
        "db-delete-09",
        "db-delete-10",
        "db-delete-11",
        "db-delete-12"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public DbDeleteTest(String filePrefix) {
    configPath = DB_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = DB_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private TransactionalScope txScope;
  private DbDelete dbDelete;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    appModel = mockApplicationModel(doc, temp);

    txScope = new TransactionalScope();
    dbDelete = new DbDelete();
    dbDelete.setApplicationModel(appModel);
    dbDelete.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), appModel));
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, txScope.getAppliedTo().getExpression())
        .forEach(node -> txScope.execute(node, report.getReport()));
    getElementsFromDocument(doc, dbDelete.getAppliedTo().getExpression())
        .forEach(node -> dbDelete.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
