/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.task.step;

import org.jdom2.Element;
import org.junit.Assert;
import org.junit.Test;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getNodesFromFile;

public class MoveNodeToParentNodeTest {

  private MoveNodeToParentNode moveNodeToParentNodeStep;

  private static final String EXAMPLE_FILE_PATH = "src/test/resources/mule/examples/http/http-all-use-case.xml";

  @Test
  public void testMoveNodeToParentNode() throws Exception {
    moveNodeToParentNodeStep = new MoveNodeToParentNode("query-param", "http",
                                                        "http://www.mulesoft.org/schema/mule/http");
    getNodesFromFile("//http:request-builder", moveNodeToParentNodeStep, EXAMPLE_FILE_PATH);
    moveNodeToParentNodeStep.execute();

    Element node = moveNodeToParentNodeStep.getNodes().get(0).getParentElement();
    int counter = 0;
    for (Element child : node.getChildren()) {
      if (child.getName().equals("query-param")) {
        counter++;
      }
    }
    Assert.assertTrue(counter > 0);
  }

  @Test
  public void testBadSource() throws Exception {
    moveNodeToParentNodeStep = new MoveNodeToParentNode("query-param1", "http",
                                                        "http://www.mulesoft.org/schema/mule/http");
    getNodesFromFile("//http:request-builder", moveNodeToParentNodeStep, EXAMPLE_FILE_PATH);
    moveNodeToParentNodeStep.execute();
    Element node = moveNodeToParentNodeStep.getNodes().get(0).getParentElement();
    Assert.assertTrue(node.getChildren().size() == 2);
  }
}
