/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.step;

import com.mulesoft.tools.migration.engine.exception.MigrationStepException;
import org.junit.Ignore;
import org.junit.Test;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getNodesFromFile;
import static org.junit.Assert.assertTrue;

@Ignore
public class AddAttributeTest {

  //  private AddAttribute attributeStep;
  //
  //  private static final String EXAMPLE_FILE_PATH = "src/test/resources/munit/examples/simple.xml";
  //
  //  @Test
  //  public void addAtributeOnNode() throws Exception {
  //    attributeStep = new AddAttribute("pepe", "lala");
  //    getNodesFromFile("//munit:assert-true", attributeStep, EXAMPLE_FILE_PATH);
  //    attributeStep.execute();
  //    assertTrue(attributeStep.getNodes().get(0).hasAttributes() == true);
  //  }
  //
  //  @Test
  //  public void addAtributeOnNodeComplexValue() throws Exception {
  //    attributeStep = new AddAttribute("pepe", "#[payload().asString()]");
  //    getNodesFromFile("//munit:assert-true", attributeStep, EXAMPLE_FILE_PATH);
  //    attributeStep.execute();
  //    assertTrue(attributeStep.getNodes().get(0).hasAttributes() == true);
  //  }
  //
  //  @Test
  //  public void addAtributeOnNodeAlreadyExistsOverwrites() throws Exception {
  //    attributeStep = new AddAttribute("pepe", "lala");
  //    getNodesFromFile("//munit:assert-true", attributeStep, EXAMPLE_FILE_PATH);
  //    attributeStep.execute();
  //    attributeStep.execute();
  //    assertTrue(attributeStep.getNodes().get(0).hasAttributes() == true);
  //  }
  //
  //  @Test(expected = MigrationStepException.class)
  //  public void addNullAttribute() throws Exception {
  //    attributeStep = new AddAttribute(null, "lala");
  //    getNodesFromFile("//munit:assert-true", attributeStep, EXAMPLE_FILE_PATH);
  //    attributeStep.execute();
  //  }
  //
  //  @Test
  //  public void addEmptyAttribute() throws Exception {
  //    attributeStep = new AddAttribute("ignore", "");
  //    getNodesFromFile("//munit:assert-true", attributeStep, EXAMPLE_FILE_PATH);
  //    attributeStep.execute();
  //    assertTrue(attributeStep.getNodes().get(0).hasAttributes() == true);
  //  }
}
