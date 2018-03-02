/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.step;

import org.jdom2.Element;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getNodesFromFile;

@Ignore
public class MoveNodeToChildNodeTest {

  //  private MoveNodeToChildNode moveNodeToChildNodeStep;
  //
  //  private static final String EXAMPLE_FILE_PATH = "src/test/resources/mule/examples/http/http-all-use-case.xml";
  //
  //  @Test
  //  public void testMoveNodeToChildNode() throws Exception {
  //    moveNodeToChildNodeStep = new MoveNodeToChildNode("response-builder", "http",
  //                                                      "http://www.mulesoft.org/schema/mule/http", "error-response-builder",
  //                                                      "http", "http://www.mulesoft.org/schema/mule/http");
  //    getNodesFromFile("//http:listener", moveNodeToChildNodeStep, EXAMPLE_FILE_PATH);
  //    moveNodeToChildNodeStep.execute();
  //
  //    Element node = moveNodeToChildNodeStep.getNodes().get(0).getChildren().get(0);
  //    for (Element child : node.getChildren()) {
  //      if (child.getNamespacePrefix().equals("response-builder")) {
  //        Assert.assertTrue(true);
  //      }
  //    }
  //  }
  //
  //  @Test
  //  public void testBadSource() throws Exception {
  //    moveNodeToChildNodeStep = new MoveNodeToChildNode("response-builder1", "http",
  //                                                      "http://www.mulesoft.org/schema/mule/http", "error-response-builder",
  //                                                      "http", "http://www.mulesoft.org/schema/mule/http");
  //    getNodesFromFile("//http:listener", moveNodeToChildNodeStep, EXAMPLE_FILE_PATH);
  //    moveNodeToChildNodeStep.execute();
  //    Element node = moveNodeToChildNodeStep.getNodes().get(0).getChildren().get(0);
  //    Assert.assertTrue(node.getChildren().size() == 2);
  //  }
  //
  //  @Test
  //  public void testBadTarget() throws Exception {
  //    moveNodeToChildNodeStep = new MoveNodeToChildNode("response-builder", "http",
  //                                                      "http://www.mulesoft.org/schema/mule/http", "error-response-builder1",
  //                                                      "http", "http://www.mulesoft.org/schema/mule/http");
  //    getNodesFromFile("//http:listener", moveNodeToChildNodeStep, EXAMPLE_FILE_PATH);
  //    moveNodeToChildNodeStep.execute();
  //    Element node = moveNodeToChildNodeStep.getNodes().get(0).getChildren().get(0);
  //    Assert.assertTrue(node.getChildren().size() == 2);
  //  }
}
