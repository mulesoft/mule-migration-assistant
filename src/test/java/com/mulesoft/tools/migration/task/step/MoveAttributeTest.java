/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.task.step;

import com.mulesoft.tools.migration.helper.DocumentHelper;
import org.jdom2.Element;
import org.junit.Test;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertNotNull;

public class MoveAttributeTest {

  private MoveAttribute moveAttribute;

  private static final String EXAMPLE_FILE_PATH = "src/test/resources/mule/examples/wsc/wsc-use-case.xml";

  @Test
  public void testMoveAttribute() throws Exception {
    moveAttribute = new MoveAttribute("mtomEnabled", "consumer-config", "ws", "http://www.mulesoft.org/schema/mule/ws",
                                      "config-ref", "name");
    DocumentHelper.getNodesFromFile("//ws:consumer", moveAttribute, EXAMPLE_FILE_PATH);
    moveAttribute.execute();
    Element node = moveAttribute.getNodes().get(0);
    assertNull(node.getAttribute("mtomEnabled"));
  }

  @Test
  public void testBadTargetNode() throws Exception {
    moveAttribute = new MoveAttribute("mtomEnabled", "consumer-config1", "ws", "http://www.mulesoft.org/schema/mule/ws",
                                      "config-ref", "name");
    DocumentHelper.getNodesFromFile("//ws:consumer", moveAttribute, EXAMPLE_FILE_PATH);
    moveAttribute.execute();
    Element node = moveAttribute.getNodes().get(0);
    assertNotNull(node.getAttribute("mtomEnabled"));
  }

  @Test
  public void testEmptyAttribute() throws Exception {
    moveAttribute = new MoveAttribute("", "consumer-config", "ws", "http://www.mulesoft.org/schema/mule/ws",
                                      "config-ref", "name");
    DocumentHelper.getNodesFromFile("//ws:consumer", moveAttribute, EXAMPLE_FILE_PATH);
    moveAttribute.execute();
    Element node = moveAttribute.getNodes().get(0);
    assertNotNull(node.getAttribute("mtomEnabled"));
  }
}
