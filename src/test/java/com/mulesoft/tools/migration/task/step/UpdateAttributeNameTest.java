/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.task.step;

import com.mulesoft.tools.migration.engine.exception.MigrationStepException;
import org.jdom2.Element;
import org.junit.Test;

import java.util.Collections;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getNodesFromFile;
import static org.junit.Assert.*;

public class UpdateAttributeNameTest {

  private UpdateAttributeName updateAttributeName;

  private static final String EXAMPLE_FILE_PATH = "src/test/resources/munit/examples/sample-file.xml";

  @Test
  public void updateNameToNonExistingAttribute() throws Exception {
    updateAttributeName = new UpdateAttributeName("condi", "pepe");
    getNodesFromFile("//munit:assert-true", updateAttributeName, EXAMPLE_FILE_PATH);
    updateAttributeName.execute();
    Element node = updateAttributeName.getNodes().get(0);
    assertNull(node.getAttribute("pepe"));
  }

  @Test
  public void updateNameToAttribute() throws Exception {
    updateAttributeName = new UpdateAttributeName("condition", "pepe");
    getNodesFromFile("//munit:assert-true", updateAttributeName, EXAMPLE_FILE_PATH);
    updateAttributeName.execute();
    Element node = updateAttributeName.getNodes().get(0);
    assertNotNull(node.getAttribute("pepe"));
  }

  @Test
  public void updateNameToAttributeToAlreadyDeclaredOne() throws Exception {
    updateAttributeName = new UpdateAttributeName("message", "level");
    getNodesFromFile("//*[contains(local-name(),'logger')]", updateAttributeName, EXAMPLE_FILE_PATH);
    updateAttributeName.execute();
    Element node = updateAttributeName.getNodes().get(0);
    assertNotNull(node.getAttribute("level"));
  }

  @Test(expected = MigrationStepException.class)
  public void updateNameToEmptyString() throws Exception {
    updateAttributeName = new UpdateAttributeName("message", "");
    getNodesFromFile("//*[contains(local-name(),'logger')]", updateAttributeName, EXAMPLE_FILE_PATH);
    updateAttributeName.execute();
    Element node = updateAttributeName.getNodes().get(0);
  }

  @Test(expected = MigrationStepException.class)
  public void updateNameToNullString() throws Exception {
    updateAttributeName = new UpdateAttributeName("message", null);
    getNodesFromFile("//*[contains(local-name(),'logger')]", updateAttributeName, EXAMPLE_FILE_PATH);
    updateAttributeName.execute();
    Element node = updateAttributeName.getNodes().get(0);
  }

  @Test
  public void executeTaskToEmptyNodeList() throws Exception {
    updateAttributeName = new UpdateAttributeName("message", "test");
    getNodesFromFile("//lala", updateAttributeName, EXAMPLE_FILE_PATH);
    updateAttributeName.execute();
    assertEquals(Collections.<Element>emptyList(), updateAttributeName.getNodes());
  }
}
