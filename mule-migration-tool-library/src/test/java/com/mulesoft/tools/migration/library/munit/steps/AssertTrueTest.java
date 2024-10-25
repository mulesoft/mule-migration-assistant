/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.munit.steps;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.tck.ReportVerification;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AssertTrueTest {

  private static final String MUNIT_SAMPLE_XML = "munit-processors.xml";
  private static final Path MUNIT_EXAMPLES_PATH = Paths.get("munit/examples");
  private static final Path MUNIT_SAMPLE_PATH = MUNIT_EXAMPLES_PATH.resolve(MUNIT_SAMPLE_XML);

  @Rule
  public ReportVerification report = new ReportVerification();

  private AssertTrue assertTrue;
  private Element node;

  @Before
  public void setUp() throws Exception {
    assertTrue = new AssertTrue();
    assertTrue.setExpressionMigrator(new MelToDwExpressionMigrator(report.getReport(), mock(ApplicationModel.class)));
  }

  @Test(expected = MigrationStepException.class)
  public void executeWithNullElement() throws Exception {
    assertTrue.execute(null, report.getReport());
  }

  @Test
  public void execute() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(MUNIT_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, assertTrue.getAppliedTo().getExpression()).get(0);
    assertTrue.execute(node, report.getReport());

    assertThat("The node didn't change", node.getName(), is("assert-that"));
    assertThat("The attribute didn't change", node.getAttribute("is"), is(notNullValue()));
    assertThat("The attribute didn't change", node.getAttribute("is").getValue(), is("#[MunitTools::equalTo(true)]"));
  }
}
