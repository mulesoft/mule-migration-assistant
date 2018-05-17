/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.library.tools.MelToDwExpressionMigrator;
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
import static org.mockito.Mockito.mock;

public class ForEachExpressionsTest {

  private static final String CHOICE_SAMPLE_XML = "forEach.xml";
  private static final Path CHOICE_EXAMPLES_PATH = Paths.get("mule/examples/core");
  private static final Path CHOICE_SAMPLE_PATH = CHOICE_EXAMPLES_PATH.resolve(CHOICE_SAMPLE_XML);

  private ForEachExpressions forEachExpressions;
  private Element node;

  @Before
  public void setUp() throws Exception {
    forEachExpressions = new ForEachExpressions();
    forEachExpressions.setExpressionMigrator(new MelToDwExpressionMigrator(mock(MigrationReport.class)));
  }


  @Test
  public void execute() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(CHOICE_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, forEachExpressions.getAppliedTo().getExpression()).get(0);
    forEachExpressions.execute(node, mock(MigrationReport.class));

    assertThat("The collection expression didn't change", node.getAttribute("collection").getValue(), is("#[vars.testVar]"));
  }

}
