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
public class DbConfigTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final Path DB_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/db");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "db-config-01",
        "db-config-02",
        "db-config-03",
        "db-config-04",
        "db-config-05",
        "db-config-06",
        "db-config-07",
        "db-config-08",
        "db-config-09",
        "db-config-10",
        "db-config-11",
        "db-config-12",
        "db-config-13",
        "db-config-14",
        "db-config-15",
        "db-config-16",
        "db-config-17",
        "db-config-18",
        "db-config-19",
        "db-config-20",
        "db-config-21"
    };
  }

  private final Path configPath;
  private final Path targetPath;

  public DbConfigTest(String filePrefix) {
    configPath = DB_CONFIG_EXAMPLES_PATH.resolve(filePrefix + "-original.xml");
    targetPath = DB_CONFIG_EXAMPLES_PATH.resolve(filePrefix + ".xml");
  }

  private DbConfig dbConfig;
  private JbossTxManager jbossTxManager;

  private Document doc;
  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    doc = getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    appModel = mockApplicationModel(doc, temp);

    dbConfig = new DbConfig();
    dbConfig.setApplicationModel(appModel);

    jbossTxManager = new JbossTxManager();
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, dbConfig.getAppliedTo().getExpression())
        .forEach(node -> dbConfig.execute(node, report.getReport()));
    getElementsFromDocument(doc, jbossTxManager.getAppliedTo().getExpression())
        .forEach(node -> jbossTxManager.execute(node, report.getReport()));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
