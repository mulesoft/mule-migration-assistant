/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.munit.steps;

import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;

public class RemoveSpringImportTest {

  private static final String MUNIT_SAMPLE_XML = "munit-processors.xml";
  private static final Path MUNIT_EXAMPLES_PATH = Paths.get("munit/examples");
  private static final Path MUNIT_SAMPLE_PATH = MUNIT_EXAMPLES_PATH.resolve(MUNIT_SAMPLE_XML);

  private RemoveSpringImport removeSpringImport;
  private Element node;

  @Before
  public void setUp() throws Exception {
    removeSpringImport = new RemoveSpringImport();
  }

  @Test
  public void execute() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(MUNIT_SAMPLE_PATH.toString()).toURI().getPath());
    doc.setBaseURI("src/test/munit/" + MUNIT_SAMPLE_XML);
    node = getElementsFromDocument(doc, removeSpringImport.getAppliedTo().getExpression()).get(0);

    assertThat("There is no spring section defined on doc.", doc.getRootElement().getChildren().size(), is(10));

    removeSpringImport.execute(node, mock(MigrationReport.class));

    assertThat("The spring section wasn't removed.", node.getParent(), nullValue());
    assertThat("The spring section wasn't removed.", doc.getRootElement().getChildren().size(), is(9));
  }

}
