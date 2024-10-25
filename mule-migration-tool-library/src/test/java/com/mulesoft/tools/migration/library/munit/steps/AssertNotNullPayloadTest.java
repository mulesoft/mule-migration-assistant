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

import com.mulesoft.tools.migration.tck.ReportVerification;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AssertNotNullPayloadTest {

  @Rule
  public ReportVerification report = new ReportVerification();

  private static final String MUNIT_SAMPLE_XML = "munit-processors.xml";
  private static final Path MUNIT_EXAMPLES_PATH = Paths.get("munit/examples");
  private static final Path MUNIT_SAMPLE_PATH = MUNIT_EXAMPLES_PATH.resolve(MUNIT_SAMPLE_XML);

  private AssertNotNullPayload assertNotNullPayload;
  private Element node;

  @Before
  public void setUp() throws Exception {
    assertNotNullPayload = new AssertNotNullPayload();
  }

  @Test
  public void executeWithNullElement() throws Exception {
    assertNotNullPayload.execute(null, report.getReport());
  }

  @Test
  public void execute() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(MUNIT_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, assertNotNullPayload.getAppliedTo().getExpression()).get(0);
    assertNotNullPayload.execute(node, report.getReport());

    assertThat("The node didn't change", node.getName(), is("assert-that"));
    assertThat("The attribute didn't change", node.getAttribute("is"), is(notNullValue()));
    assertThat("The attribute didn't change", node.getAttribute("is").getValue(), is("#[MunitTools::notNullValue()]"));
  }
}
