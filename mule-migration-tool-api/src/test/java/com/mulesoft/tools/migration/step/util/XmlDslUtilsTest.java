/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.step.util;

import org.jdom2.Comment;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getDocument;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.removeNestedComments;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class XmlDslUtilsTest {

  private static final Path UTILS_EXAMPLES_PATH = Paths.get("util");
  private static final String ELEMENT_NAME = "sample-top-level";

  private Element newTopLevelElement;
  private Document document;

  @Before
  public void setUp() throws Exception {
    newTopLevelElement = new Element(ELEMENT_NAME);
  }

  @Test
  public void addTopLevelElementTest1() throws Exception {
    document = getDocumentWithName("top-level-elements-1.xml");
    addTopLevelElement(newTopLevelElement, document);
    assertThat("Wrong position.", document.getRootElement().getChildren().get(3).getName(), equalTo(ELEMENT_NAME));
  }

  @Test
  public void addTopLevelElementTest2() throws Exception {
    document = getDocumentWithName("top-level-elements-2.xml");
    addTopLevelElement(newTopLevelElement, document);
    assertThat("Wrong position.", document.getRootElement().getChildren().get(0).getName(), equalTo(ELEMENT_NAME));
  }

  @Test
  public void addTopLevelElementTest3() throws Exception {
    document = getDocumentWithName("top-level-elements-3.xml");
    addTopLevelElement(newTopLevelElement, document);
    assertThat("Wrong position.", document.getRootElement().getChildren().get(1).getName(), equalTo(ELEMENT_NAME));
  }

  @Test
  public void addTopLevelElementTest4() throws Exception {
    document = getDocumentWithName("top-level-elements-4.xml");
    addTopLevelElement(newTopLevelElement, document);
    assertThat("Wrong position.", document.getRootElement().getChildren().get(0).getName(), equalTo(ELEMENT_NAME));
  }

  @Test
  public void removeNestedComment() throws Exception {
    document = getDocumentWithName("element-with-comment.xml");
    Element root = document.getRootElement();
    assertTrue("Root element has not nested comments",
               root.getChildren().get(0).getContent().stream().anyMatch(content -> content instanceof Comment));
    removeNestedComments(root);
    assertTrue("Root element has nested comments",
               root.getChildren().get(0).getContent().stream().noneMatch(content -> content instanceof Comment));
  }

  private Document getDocumentWithName(String fileName) throws Exception {
    return getDocument(this.getClass().getClassLoader()
        .getResource(UTILS_EXAMPLES_PATH.resolve(fileName).toString()).toURI().getPath());
  }
}
