/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

public class ForEachScopeTest {

  private static final String FILE_SAMPLE_XML = "forEachScope.xml";
  private static final String REMOVE_JSON_TRANSFORMER_NAME = "json-to-object-transformer";
  private static final String REMOVE_BYTE_ARRAY_TRANSFORMER_NAME = "byte-array-to-object-transformer";
  private static final Path FILE_EXAMPLES_PATH = Paths.get("mule/examples/core");
  private static final Path FILE_SAMPLE_PATH = FILE_EXAMPLES_PATH.resolve(FILE_SAMPLE_XML);

  private ForEachScope forEachScope;
  private Element node;

  @Before
  public void setUp() throws Exception {
    forEachScope = new ForEachScope();
  }

  @Test(expected = MigrationStepException.class)
  public void executeWithNullElement() throws Exception {
    forEachScope.execute(null);
  }

  @Test
  public void executeWithJsonTransFormer() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(FILE_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, forEachScope.getAppliedTo().getExpression()).get(0);
    forEachScope.execute(node);

    Element parent = node.getParentElement();
    assertThat("The node didn't change", parent.getChildren(REMOVE_JSON_TRANSFORMER_NAME), is(empty()));
  }

  @Test
  public void executeWithByteArrayTransFormer() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(FILE_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, forEachScope.getAppliedTo().getExpression()).get(2);
    forEachScope.execute(node);

    Element parent = node.getParentElement();
    assertThat("The node didn't change", parent.getChildren(REMOVE_BYTE_ARRAY_TRANSFORMER_NAME), is(empty()));
  }

  @Test
  public void executeWithNoTransformerToRemoveNotFail() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(FILE_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, forEachScope.getAppliedTo().getExpression()).get(1);
    forEachScope.execute(node);
  }

}
