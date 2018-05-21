/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.db;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

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
public class DbConfigTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

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
        "db-config-19"
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

  private ApplicationModel appModel;

  @Before
  public void setUp() throws Exception {
    dbConfig = new DbConfig();
    appModel = mock(ApplicationModel.class);
    when(appModel.getProjectBasePath()).thenReturn(temp.newFolder().toPath());

    dbConfig.setApplicationModel(appModel);

    jbossTxManager = new JbossTxManager();
  }

  @Test
  public void execute() throws Exception {
    Document doc =
        getDocument(this.getClass().getClassLoader().getResource(configPath.toString()).toURI().getPath());
    getElementsFromDocument(doc, dbConfig.getAppliedTo().getExpression())
        .forEach(node -> dbConfig.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, jbossTxManager.getAppliedTo().getExpression())
        .forEach(node -> jbossTxManager.execute(node, mock(MigrationReport.class)));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
