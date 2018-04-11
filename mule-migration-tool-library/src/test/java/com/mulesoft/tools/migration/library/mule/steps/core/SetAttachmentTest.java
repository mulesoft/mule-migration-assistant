/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.helper.DocumentHelper.getElementsFromDocument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SetAttachmentTest {

  private static final String SET_ATT_SAMPLE_XML = "setAttachment.xml";
  private static final Path SET_ATT_EXAMPLES_PATH = Paths.get("mule/examples/core");
  private static final Path SET_ATT_SAMPLE_PATH = SET_ATT_EXAMPLES_PATH.resolve(SET_ATT_SAMPLE_XML);

  private SetAttachment setAttachment;
  private Element node;

  @Before
  public void setUp() throws Exception {
    setAttachment = new SetAttachment();
  }

  @Test(expected = MigrationStepException.class)
  public void executeWithNullElement() throws Exception {
    setAttachment.execute(null, mock(MigrationReport.class));
  }

  @Test
  public void execute() throws Exception {
    Document doc = getDocument(this.getClass().getClassLoader().getResource(SET_ATT_SAMPLE_PATH.toString()).toURI().getPath());
    node = getElementsFromDocument(doc, setAttachment.getAppliedTo().getExpression()).get(0);
    setAttachment.execute(node, mock(MigrationReport.class));

    assertThat("The node didn't change", node.getName(), is("set-variable"));
    assertThat("The attribute wasn't rename", node.getAttribute("attachmentName"), is(nullValue()));
    assertThat("The attribute name is not the expected", node.getAttribute("variableName"), is(notNullValue()));
  }
}
