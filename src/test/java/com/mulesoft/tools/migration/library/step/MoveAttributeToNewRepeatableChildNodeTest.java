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

import java.util.List;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getNodesFromFile;
import static org.junit.Assert.fail;

@Ignore
public class MoveAttributeToNewRepeatableChildNodeTest {

  private static final String ATTRIBUTE = "connectionTimeout";
  private static final String NEW_ATTRIBUTE_NAME = "key";
  private static final String NEW_ATTRIBUTE_VALUE = "value";
  private MoveAttributeToNewRepeatableChildNode moveAttributeToNewRepeatableChildNode;

  private static final String EXAMPLE_FILE_PATH_ONE_DB_PROPERTY =
      "src/test/resources/mule/examples/db/connection-properties-with-one-db-property-use-case.xml";
  private static final String EXAMPLE_FILE_PATH_WITH_ZERO_DB_PROPERTIES =
      "src/test/resources/mule/examples/db/connection-properties-with-zero-db-properties-use-case.xml";
  private static final String EXAMPLE_FILE_PATH_WITH_ATTRIBUTE_PROPERTY =
      "src/test/resources/mule/examples/db/connection-properties-with-connectionTimeout-property-use-case.xml";

  @Test
  public void testMoveAttributeToNewRepeatableChildNodeWithOneProperty() throws Exception {
    moveAttributeToNewRepeatableChildNode = new MoveAttributeToNewRepeatableChildNode(ATTRIBUTE, "property",
                                                                                      NEW_ATTRIBUTE_NAME, NEW_ATTRIBUTE_VALUE);
    getNodesFromFile("//db:connection-properties", moveAttributeToNewRepeatableChildNode, EXAMPLE_FILE_PATH_ONE_DB_PROPERTY);
    moveAttributeToNewRepeatableChildNode.execute();
    final List<Element> children = moveAttributeToNewRepeatableChildNode.getNodes().get(0).getChildren();
    if (children.size() == 2) {
      Element node = children.get(1);
      if (!node.getAttribute(NEW_ATTRIBUTE_NAME).getValue().equals(ATTRIBUTE)
          || !node.getAttribute(NEW_ATTRIBUTE_VALUE).getValue().equals("1000")) {
        fail("Could not find " + ATTRIBUTE + "attribute");
      }
    } else {
      fail("There should only be two <db:property> nodes");
    }
  }

  @Test
  public void testMoveAttributeToNewRepeatableChildNodeWithZeroProperties() throws Exception {
    moveAttributeToNewRepeatableChildNode = new MoveAttributeToNewRepeatableChildNode(ATTRIBUTE, "property",
                                                                                      NEW_ATTRIBUTE_NAME, NEW_ATTRIBUTE_VALUE);
    getNodesFromFile("//db:connection-properties", moveAttributeToNewRepeatableChildNode,
                     EXAMPLE_FILE_PATH_WITH_ZERO_DB_PROPERTIES);
    moveAttributeToNewRepeatableChildNode.execute();
    final List<Element> children = moveAttributeToNewRepeatableChildNode.getNodes().get(0).getChildren();
    if (children.size() == 1) {
      Element node = children.get(0);
      if (!node.getAttribute(NEW_ATTRIBUTE_NAME).getValue().equals(ATTRIBUTE)
          || !node.getAttribute(NEW_ATTRIBUTE_VALUE).getValue().equals("1000")) {
        fail("Could not find " + ATTRIBUTE + "attribute");
      }
    } else {
      fail("There should only one <db:property> node");
    }
  }

  @Test
  public void testMoveAttributeToNewRepeatableChildNode() throws Exception {
    moveAttributeToNewRepeatableChildNode = new MoveAttributeToNewRepeatableChildNode(ATTRIBUTE, "property",
                                                                                      NEW_ATTRIBUTE_NAME, NEW_ATTRIBUTE_VALUE);
    getNodesFromFile("//db:connection-properties", moveAttributeToNewRepeatableChildNode,
                     EXAMPLE_FILE_PATH_WITH_ATTRIBUTE_PROPERTY);
    moveAttributeToNewRepeatableChildNode.execute();
    final List<Element> children = moveAttributeToNewRepeatableChildNode.getNodes().get(0).getChildren();
    if (children.size() == 1) {
      Element node = children.get(0);
      if (!node.getAttribute(NEW_ATTRIBUTE_NAME).getValue().equals(ATTRIBUTE)
          || !node.getAttribute(NEW_ATTRIBUTE_VALUE).getValue().equals("1000")) {
        fail("Could not find " + ATTRIBUTE + "attribute");
      }
    } else {
      fail("There should only one <db:property> node");
    }
  }
}
