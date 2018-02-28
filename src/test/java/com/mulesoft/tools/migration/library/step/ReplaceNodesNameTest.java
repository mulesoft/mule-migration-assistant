/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.step;

import org.jdom2.Element;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getNodesFromFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Ignore
public class ReplaceNodesNameTest {

  private ReplaceNodesName replaceQName;

  private static final String EXAMPLE_FILE_PATH = "src/test/resources/munit/examples/sample-file.xml";

  @Test
  public void replaceQNameTestNodes() throws Exception {
    replaceQName = new ReplaceNodesName("munit", "test2");
    getNodesFromFile("//munit:test", replaceQName, EXAMPLE_FILE_PATH);
    replaceQName.execute();
    String newName = replaceQName.getNodes().get(0).getName();
    assertEquals("test2", newName);
  }

  @Test
  public void replaceQNameSubChildNodes() throws Exception {
    replaceQName = new ReplaceNodesName("mock", "mock");
    getNodesFromFile("//mock:when", replaceQName, EXAMPLE_FILE_PATH);
    replaceQName.execute();
    String newName = replaceQName.getNodes().get(0).getName();
    assertEquals("mock", newName);
  }

  @Test
  public void replaceQNameNotFoundNameSpace() throws Exception {
    replaceQName = new ReplaceNodesName("lalero", "test");
    getNodesFromFile("//mock:when", replaceQName, EXAMPLE_FILE_PATH);
    replaceQName.execute();
    String newName = replaceQName.getNodes().get(0).getName();
    assertEquals("when", newName);
  }

  @Test
  public void replaceQNameEmptyNodes() throws Exception {
    replaceQName = new ReplaceNodesName("munit", "lala");
    getNodesFromFile("//mock:when2423", replaceQName, EXAMPLE_FILE_PATH);
    replaceQName.execute();
    assertTrue(replaceQName.getNodes() == Collections.<Element>emptyList());
  }
}
