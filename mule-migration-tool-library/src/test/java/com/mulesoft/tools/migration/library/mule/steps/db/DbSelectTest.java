/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
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
public class DbSelectTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path DB_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/db");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "db-select-01",
        "db-select-02",
        "db-select-03",
        "db-select-04",
        "db-select-05",
        "db-select-06",
        "db-select-07",
        "db-select-08",
        "db-select-09",
        "db-select-10",
        "db-select-11",
        "db-select-12",
        "db-select-13",
        "db-select-14"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public DbSelectTest(String filePrefix) {
    configPath = DB_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = DB_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private TransactionalScope txScope;
  private DbSelect dbSelect;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    appModel = mockApplicationModel(doc, temp);

    txScope = new TransactionalScope();
    dbSelect = new DbSelect();
    dbSelect.setApplicationModel(appModel);
    dbSelect.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), appModel));
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, txScope.getAppliedTo().getExpression())
        .forEach(node -> txScope.execute(node, report.getReport()));
    getElementsFromDocument(doc, dbSelect.getAppliedTo().getExpression())
        .forEach(node -> dbSelect.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
