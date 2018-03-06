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
public class DeleteChildNodeTest {

  //  private DeleteChildNode deleteChildNodeStep;
  //
  //  private static final String EXAMPLE_FILE_PATH = "src/test/resources/mule/examples/http/http-all-use-case.xml";
  //
  //  @Test
  //  public void testMoveNodeToParentNode() throws Exception {
  //    deleteChildNodeStep = new DeleteChildNode("request-builder", "http",
  //                                              "http://www.mulesoft.org/schema/mule/http");
  //    getNodesFromFile("//http:request", deleteChildNodeStep, EXAMPLE_FILE_PATH);
  //    deleteChildNodeStep.execute();
  //    Element node = deleteChildNodeStep.getNodes().get(0);
  //    Assert.assertTrue(node.getChildren().size() == 1);
  //  }
  //
  //  @Test
  //  public void testBadSource() throws Exception {
  //    deleteChildNodeStep = new DeleteChildNode("request-builder1", "http",
  //                                              "http://www.mulesoft.org/schema/mule/http");
  //    getNodesFromFile("//http:request", deleteChildNodeStep, EXAMPLE_FILE_PATH);
  //    deleteChildNodeStep.execute();
  //    Element node = deleteChildNodeStep.getNodes().get(0);
  //    Assert.assertTrue(node.getChildren().size() == 2);
  //  }
}
