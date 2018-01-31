/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.task.step;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Test;

import java.util.List;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getNodesFromFile;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class MoveAttributeToNewChildNodeTest {

    private MoveAttributeToNewChildNode moveAttStep;

    private static final String EXAMPLE_FILE_PATH = "src/test/resources/munit/examples/sample-file.xml";

    @Test
    public void moveToChildNode() throws Exception {
        moveAttStep = new MoveAttributeToNewChildNode("messageProcessor", "with-attributes");
        getNodesFromFile("//mock:when", moveAttStep, EXAMPLE_FILE_PATH);
        moveAttStep.execute();
        Element node = moveAttStep.getNodes().get(0);
        assertNotNull(node.getChildren().get(0).getAttribute("messageProcessor"));
    }

    @Test
    public void moveAttributeToNotDefinedChildNode() throws Exception {
        moveAttStep = new MoveAttributeToNewChildNode("messageProcessor", "pepe");
        getNodesFromFile("//mock:when", moveAttStep, EXAMPLE_FILE_PATH);
        moveAttStep.execute();
        Element node = moveAttStep.getNodes().get(0);
        final List<Element> children = node.getChildren();
        assertNotNull(node.getChild("pepe", Namespace.getNamespace("mock", "http://www.mulesoft.org/schema/mule/mock")));
    }

    @Test
    public void moveAttributeNotExistsOnNode() throws Exception {
        moveAttStep = new MoveAttributeToNewChildNode("pepe", "with-attributes");
        getNodesFromFile("//mock:when", moveAttStep, EXAMPLE_FILE_PATH);
        moveAttStep.execute();
        Element node = moveAttStep.getNodes().get(0);
        assertNull(node.getChildren().get(0).getAttribute("pepe"));
    }
}
