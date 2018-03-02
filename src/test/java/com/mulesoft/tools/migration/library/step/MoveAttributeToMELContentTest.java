/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.step;

import org.junit.Ignore;
import org.junit.Test;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getNodesFromFile;
import static org.junit.Assert.assertTrue;

@Ignore
public class MoveAttributeToMELContentTest {

  //  private MoveAttributeToMELContent moveAttributeToMELContentStep;
  //
  //  private static final String EXAMPLE_FILE_PATH = "src/test/resources/mule/examples/http/http-all-use-case.xml";
  //
  //  @Test
  //  public void testMoveAttributeToMELContent() throws Exception {
  //    moveAttributeToMELContentStep = new MoveAttributeToMELContent("expression");
  //    getNodesFromFile("//http:uri-params", moveAttributeToMELContentStep, EXAMPLE_FILE_PATH);
  //    moveAttributeToMELContentStep.execute();
  //    assertTrue(moveAttributeToMELContentStep.getNodes().get(0).getText().contains("mel"));
  //  }
  //
  //  @Test
  //  public void testBadAttribute() throws Exception {
  //    moveAttributeToMELContentStep = new MoveAttributeToMELContent("expression1");
  //    getNodesFromFile("//http:uri-params", moveAttributeToMELContentStep, EXAMPLE_FILE_PATH);
  //    moveAttributeToMELContentStep.execute();
  //    assertTrue(!moveAttributeToMELContentStep.getNodes().get(0).getText().contains("mel"));
  //  }
}
