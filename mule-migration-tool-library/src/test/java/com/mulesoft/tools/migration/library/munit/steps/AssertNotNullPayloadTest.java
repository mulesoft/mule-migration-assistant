/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.munit.steps;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AssertNotNullPayloadTest {

  private static final String MUNIT_SAMPLE_XML = "munit-processors.xml";
  private static final Path MUNIT_EXAMPLES_PATH = Paths.get("munit/examples");
  private static final Path MUNIT_SAMPLE_PATH = MUNIT_EXAMPLES_PATH.resolve(MUNIT_SAMPLE_XML);

  private AssertNotNullPayload assertNotNullPayload;
  private Element node;

  @Before
  public void setUp() throws Exception {
    assertNotNullPayload = new AssertNotNullPayload();
  }

  @Test(expected = MigrationStepException.class)
  public void executeWithNullElement() throws Exception {
    assertNotNullPayload.execute(null, mock(MigrationReport.class));
  }

  @Test
  public void execute() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(MUNIT_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, assertNotNullPayload.getAppliedTo().getExpression()).get(0);
    assertNotNullPayload.execute(node, mock(MigrationReport.class));

    assertThat("The node didn't change", node.getName(), is("assert-that"));
    assertThat("The attribute didn't change", node.getAttribute("is"), is(notNullValue()));
    assertThat("The attribute didn't change", node.getAttribute("is").getValue(), is("#[MunitTools::notNullValue()]"));
  }
}
