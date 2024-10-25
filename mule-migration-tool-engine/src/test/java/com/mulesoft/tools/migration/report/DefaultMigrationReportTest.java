/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.report;

import com.mulesoft.tools.migration.library.munit.steps.AssertTrue;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class DefaultMigrationReportTest {

  private static final String MUNIT_SAMPLE_XML = "munit-sections-sample.xml";
  private static final Path MUNIT_EXAMPLES_PATH = Paths.get("munit/examples");
  private static final Path MUNIT_SAMPLE_PATH = MUNIT_EXAMPLES_PATH.resolve(MUNIT_SAMPLE_XML);

  private DefaultMigrationReport defaultMigrationReport;
  private AssertTrue assertTrue;

  @Before
  public void setUp() throws Exception {
    defaultMigrationReport = new DefaultMigrationReport();
    assertTrue = new AssertTrue();
    assertTrue.setExpressionMigrator(new MelToDwExpressionMigrator(mock(MigrationReport.class), mock(ApplicationModel.class)));
  }

  @Test
  public void notAddDuplicateEntryTest() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(MUNIT_SAMPLE_PATH.toString()).toURI().getPath());
    Element node = getElementsFromDocument(doc, assertTrue.getAppliedTo().getExpression()).get(0);

    defaultMigrationReport.report(WARN, node, node, "Message", "docLink");
    defaultMigrationReport.report(WARN, node, node, "Message2", "newLink");
    defaultMigrationReport.report(WARN, node, node, "Message", "docLink");

    assertThat("Duplicate entry added.", defaultMigrationReport.getReportEntries().size(), is(2));
  }

}
