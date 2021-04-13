/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.tck.ReportVerification;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ForEachScopeTest {

  private static final String FILE_SAMPLE_XML = "forEachScope.xml";
  private static final String REMOVE_JSON_TRANSFORMER_NAME = "json-to-object-transformer";
  private static final String REMOVE_BYTE_ARRAY_TRANSFORMER_NAME = "byte-array-to-object-transformer";
  private static final Path FILE_EXAMPLES_PATH = Paths.get("mule/examples/core");
  private static final Path FILE_SAMPLE_PATH = FILE_EXAMPLES_PATH.resolve(FILE_SAMPLE_XML);

  @Rule
  public ReportVerification report = new ReportVerification();

  private ForEachScope forEachScope;
  private Element node;

  @Before
  public void setUp() throws Exception {
    forEachScope = new ForEachScope();
  }

  @Test(expected = MigrationStepException.class)
  public void executeWithNullElement() throws Exception {
    forEachScope.execute(null, report.getReport());
  }

  @Test
  public void executeWithJsonTransFormer() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(FILE_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, forEachScope.getAppliedTo().getExpression()).get(0);
    forEachScope.execute(node, report.getReport());

    Element parent = node.getParentElement();
    assertThat("The node didn't change", parent.getChildren(REMOVE_JSON_TRANSFORMER_NAME), is(empty()));
  }

  @Test
  public void executeWithByteArrayTransFormer() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(FILE_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, forEachScope.getAppliedTo().getExpression()).get(2);
    forEachScope.execute(node, report.getReport());

    Element parent = node.getParentElement();
    assertThat("The node didn't change", parent.getChildren(REMOVE_BYTE_ARRAY_TRANSFORMER_NAME), is(empty()));
  }

  @Test
  public void executeWithNoTransformerToRemoveNotFail() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(FILE_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, forEachScope.getAppliedTo().getExpression()).get(1);
    forEachScope.execute(node, report.getReport());
  }

}
