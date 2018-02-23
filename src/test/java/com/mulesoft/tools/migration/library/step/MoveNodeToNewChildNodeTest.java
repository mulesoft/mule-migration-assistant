/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.step;

import org.jdom2.Element;
import org.junit.Assert;
import org.junit.Test;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getNodesFromFile;

public class MoveNodeToNewChildNodeTest {

  private MoveNodeToNewChildNode moveNodeToNewChildNodeStep;

  private static final String EXAMPLE_FILE_PATH = "src/test/resources/mule/examples/http/http-all-use-case.xml";

  @Test
  public void testMoveNodeToChildNode() throws Exception {
    moveNodeToNewChildNodeStep =
        new MoveNodeToNewChildNode("response-builder", "http", "http://www.mulesoft.org/schema/mule/http", "new-target", "http",
                                   "http://www.mulesoft.org/schema/mule/http");
    getNodesFromFile("//http:listener", moveNodeToNewChildNodeStep, EXAMPLE_FILE_PATH);
    moveNodeToNewChildNodeStep.execute();

    Element node = moveNodeToNewChildNodeStep.getNodes().get(0).getChildren().get(0);
    for (Element child : node.getChildren()) {
      if (child.getNamespacePrefix().equals("response-builder")) {
        Assert.assertTrue(true);
      }
    }
  }

  @Test
  public void testBadSource() throws Exception {
    moveNodeToNewChildNodeStep =
        new MoveNodeToNewChildNode("response-builder1", "http", "http://www.mulesoft.org/schema/mule/http", "new-target", "http",
                                   "http://www.mulesoft.org/schema/mule/http");
    getNodesFromFile("//http:listener", moveNodeToNewChildNodeStep, EXAMPLE_FILE_PATH);
    moveNodeToNewChildNodeStep.execute();

    Element node = moveNodeToNewChildNodeStep.getNodes().get(0).getChildren().get(0);
    Assert.assertTrue(node.getChildren().size() == 2);
  }
}
