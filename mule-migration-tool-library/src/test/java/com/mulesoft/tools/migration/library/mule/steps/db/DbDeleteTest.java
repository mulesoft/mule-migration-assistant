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

import com.mulesoft.tools.migration.library.mule.steps.core.TransactionalScope;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(Parameterized.class)
public class DbDeleteTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  private static final Path DB_CONFIG_EXAMPLES_PATH = Paths.get("mule/apps/db");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "db-delete-01",
        "db-delete-02",
        // TODO MMT-128
        // "db-delete-03",
        "db-delete-04",
        "db-delete-05",
        "db-delete-06",
        "db-delete-07",
        // TODO MMT-128
        // "db-delete-08",
        "db-delete-09",
        "db-delete-10",
        "db-delete-11",
        // TODO MMT-128
        // "db-delete-12"
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

    txScope = new TransactionalScope();
    dbDelete = new DbDelete();
    appModel = mock(ApplicationModel.class);
    when(appModel.getNodes(Mockito.any(XPathExpression.class)))
        .thenAnswer(invocation -> getElementsFromDocument(doc,
                                                          ((XPathExpression) (invocation.getArguments()[0])).getExpression()));
    when(appModel.getProjectBasePath()).thenReturn(temp.newFolder().toPath());

    dbDelete.setApplicationModel(appModel);
    dbDelete.setExpressionMigrator(new MelToDwExpressionMigrator(mock(MigrationReport.class)));
  }

  @Test
  public void execute() throws Exception {
    getElementsFromDocument(doc, txScope.getAppliedTo().getExpression())
        .forEach(node -> txScope.execute(node, mock(MigrationReport.class)));
    getElementsFromDocument(doc, dbDelete.getAppliedTo().getExpression())
        .forEach(node -> dbDelete.execute(node, mock(MigrationReport.class)));

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    String xmlString = outputter.outputString(doc);

    assertThat(xmlString,
               isSimilarTo(IOUtils.toString(this.getClass().getClassLoader().getResource(targetPath.toString()).toURI(), UTF_8))
                   .ignoreComments().normalizeWhitespace());
  }

}
