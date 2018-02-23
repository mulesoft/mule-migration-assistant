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

public class DeleteNamespaceTest {

  private DeleteNamespace deleteNamespaceStep;

  private static final String EXAMPLE_FILE_PATH = "src/test/resources/mule/examples/http/http-all-use-case.xml";

  @Test
  @Ignore
  public void testBadNamespace() throws Exception {
    deleteNamespaceStep = new DeleteNamespace("a", "http://www.mulesoft.org/schema/mule/http",
                                              "http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd");
    getNodesFromFile("//mule", deleteNamespaceStep, EXAMPLE_FILE_PATH);
    deleteNamespaceStep.execute();
    assertTrue(deleteNamespaceStep.getDocument().getRootElement().getNamespace("http") != null);
  }

  @Test
  public void testBadNamespaceUri() throws Exception {
    deleteNamespaceStep = new DeleteNamespace("http", "b",
                                              "http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd");
    getNodesFromFile("//mule", deleteNamespaceStep, EXAMPLE_FILE_PATH);
    deleteNamespaceStep.execute();
    assertTrue(deleteNamespaceStep.getDocument().getRootElement().getNamespace("http") != null);
  }

  @Test
  @Ignore
  public void testBadSchemaLocationUrl() throws Exception {
    deleteNamespaceStep = new DeleteNamespace("http", "http://www.mulesoft.org/schema/mule/http", "c");
    getNodesFromFile("//mule", deleteNamespaceStep, EXAMPLE_FILE_PATH);
    deleteNamespaceStep.execute();
    assertTrue(deleteNamespaceStep.getDocument().getRootElement().getNamespace("http") != null);
  }

  @Test
  public void testBlankValues() throws Exception {
    deleteNamespaceStep = new DeleteNamespace("", "", "");
    getNodesFromFile("//mule", deleteNamespaceStep, EXAMPLE_FILE_PATH);
    deleteNamespaceStep.execute();
    assertTrue(deleteNamespaceStep.getDocument().getRootElement().getNamespace("http") != null);
  }

  @Test
  public void testDeleteNamespace() throws Exception {
    deleteNamespaceStep = new DeleteNamespace("http", "http://www.mulesoft.org/schema/mule/http",
                                              "http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd");
    getNodesFromFile("//mule", deleteNamespaceStep, EXAMPLE_FILE_PATH);
    deleteNamespaceStep.execute();
    assertTrue(deleteNamespaceStep.getDocument().getRootElement().getNamespace("http") == null);
  }
}
