/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.step;

import org.junit.Ignore;

@Ignore
public class MoveAttributeToChildNodeTest {

  //  private MoveAttributeToChildNode moveAttStep;
  //
  //  private static final String EXAMPLE_FILE_PATH = "src/test/resources/munit/examples/sample-file.xml";
  //
  //  @Test
  //  public void moveToChildNode() throws Exception {
  //    moveAttStep = new MoveAttributeToChildNode("messageProcessor", "with-attributes");
  //    getNodesFromFile("//mock:when", moveAttStep, EXAMPLE_FILE_PATH);
  //    moveAttStep.execute();
  //    Element node = moveAttStep.getNodes().get(0);
  //    assertNotNull(node.getChildren().get(0).getAttribute("messageProcessor"));
  //  }
  //
  //  @Test
  //  public void moveAttributeToNotDefinedChildNode() throws Exception {
  //    moveAttStep = new MoveAttributeToChildNode("messageProcessor", "pepe");
  //    getNodesFromFile("//mock:when", moveAttStep, EXAMPLE_FILE_PATH);
  //    moveAttStep.execute();
  //    Element node = moveAttStep.getNodes().get(0);
  //    assertNull(node.getChild("pepe", Namespace.getNamespace("mock", "http://www.mulesoft.org/schema/mule/mock")));
  //  }
  //
  //  @Test
  //  public void moveAttributeNotExistsOnNode() throws Exception {
  //    moveAttStep = new MoveAttributeToChildNode("pepe", "with-attributes");
  //    getNodesFromFile("//mock:when", moveAttStep, EXAMPLE_FILE_PATH);
  //    moveAttStep.execute();
  //    Element node = moveAttStep.getNodes().get(0);
  //    assertNull(node.getChildren().get(0).getAttribute("pepe"));
  //  }
}
