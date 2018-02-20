/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.task.step;

import org.junit.Test;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getNodesFromFile;
import static org.junit.Assert.*;

public class CreateChildNodeFromAttributeTest {

  private CreateChildNodeFromAttribute createAttNodeStep;

  private static final String EXAMPLE_FILE_PATH = "src/test/resources/munit/examples/simple.xml";

  @Test
  public void setAttributeNotExists() throws Exception {
    createAttNodeStep = new CreateChildNodeFromAttribute("lala");
    getNodesFromFile("//munit:assert-true", createAttNodeStep, EXAMPLE_FILE_PATH);
    createAttNodeStep.execute();
    assertTrue(createAttNodeStep.getNodes().get(0).getChildren().size() == 0);
  }

  @Test
  public void createWithEmptyAttributeValue() throws Exception {
    createAttNodeStep = new CreateChildNodeFromAttribute("");
    getNodesFromFile("//munit:assert-true", createAttNodeStep, EXAMPLE_FILE_PATH);
    createAttNodeStep.execute();
    assertTrue(createAttNodeStep.getNodes().get(0).getChildren().size() == 0);
  }

  @Test
  public void createSimpleNodeFromAttribute() throws Exception {
    createAttNodeStep = new CreateChildNodeFromAttribute("prop");
    getNodesFromFile("//munit:assert-true", createAttNodeStep, EXAMPLE_FILE_PATH);
    createAttNodeStep.execute();
    assertTrue(createAttNodeStep.getNodes().get(0).getChildren().size() == 1);
  }

}
