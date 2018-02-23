/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.task.step;

import com.mulesoft.tools.migration.engine.exception.MigrationStepException;
import com.mulesoft.tools.migration.helper.DocumentHelper;
import org.jdom2.Element;
import org.junit.Test;

import static org.junit.Assert.*;

public class UpdateAttributeTest {

  private UpdateAttribute updateAttribute;

  private static final String EXAMPLE_FILE_PATH = "src/test/resources/munit/examples/sample-file.xml";

  @Test
  public void updateValueToNonExistingAttribute() throws Exception {
    updateAttribute = new UpdateAttribute("condi", "lala");
    DocumentHelper.getNodesFromFile("//munit:assert-true", updateAttribute, EXAMPLE_FILE_PATH);
    updateAttribute.execute();
    Element node = updateAttribute.getNodes().get(0);
    assertNull(node.getAttribute("pepe"));
  }

  @Test
  public void updateSimpleValue() throws Exception {
    updateAttribute = new UpdateAttribute("condition", "lala");
    DocumentHelper.getNodesFromFile("//munit:assert-true", updateAttribute, EXAMPLE_FILE_PATH);
    updateAttribute.execute();
    Element node = updateAttribute.getNodes().get(0);
    assertEquals("lala", node.getAttributeValue("condition"));
  }

  @Test
  public void updateValueToComplex() throws Exception {
    updateAttribute = new UpdateAttribute("condition", "#[message.flowVars('pepe')]");
    DocumentHelper.getNodesFromFile("//munit:assert-true", updateAttribute, EXAMPLE_FILE_PATH);
    updateAttribute.execute();
    Element node = updateAttribute.getNodes().get(0);
    assertEquals("#[message.flowVars('pepe')]", node.getAttributeValue("condition"));
  }

  @Test
  public void updateValueToPlaceholder() throws Exception {
    updateAttribute = new UpdateAttribute("condition", "${myprop}");
    DocumentHelper.getNodesFromFile("//munit:assert-true", updateAttribute, EXAMPLE_FILE_PATH);
    updateAttribute.execute();
    Element node = updateAttribute.getNodes().get(0);
    assertEquals("${myprop}", node.getAttributeValue("condition"));
  }

  @Test(expected = MigrationStepException.class)
  public void updateValueToNull() throws Exception {
    updateAttribute = new UpdateAttribute("condition", null);
    DocumentHelper.getNodesFromFile("//munit:assert-true", updateAttribute, EXAMPLE_FILE_PATH);
    updateAttribute.execute();
    Element node = updateAttribute.getNodes().get(0);
  }

  @Test
  public void updateValueToEmpty() throws Exception {
    updateAttribute = new UpdateAttribute("condition", "");
    DocumentHelper.getNodesFromFile("//munit:assert-true", updateAttribute, EXAMPLE_FILE_PATH);
    updateAttribute.execute();
    Element node = updateAttribute.getNodes().get(0);
    assertEquals("", node.getAttributeValue("condition"));
  }
}
