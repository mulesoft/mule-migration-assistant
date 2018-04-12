/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.munit.steps;

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
import static org.hamcrest.Matchers.is;

public class MoveMUnitProcessorsToSectionsTest {

  private static final String MUNIT_SAMPLE_XML = "munit-move-processors.xml";
  private static final Path MUNIT_EXAMPLES_PATH = Paths.get("munit/examples");
  private static final Path MUNIT_SAMPLE_PATH = MUNIT_EXAMPLES_PATH.resolve(MUNIT_SAMPLE_XML);

  private MoveMUnitProcessorsToSections moveMUnitProcessorsToSections;
  private Element node;

  @Before
  public void setUp() throws Exception {
    moveMUnitProcessorsToSections = new MoveMUnitProcessorsToSections();
  }

  @Test(expected = MigrationStepException.class)
  public void executeWithNullElement() throws Exception {
    moveMUnitProcessorsToSections.execute(null);
  }

  @Test
  public void execute() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(MUNIT_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, moveMUnitProcessorsToSections.getAppliedTo().getExpression()).get(0);
    moveMUnitProcessorsToSections.execute(node);

    assertThat("The munit test not contains sections.", node.getChildren().size(), is(3));
    assertThat("The munit test not contains sections.", node.getChildren().get(0).getName(), is("behavior"));
    assertThat("The munit test not contains sections.", node.getChildren().get(1).getName(), is("execution"));
    assertThat("The munit test not contains sections.", node.getChildren().get(2).getName(), is("validation"));
  }
}
