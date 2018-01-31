/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.task.step;

import com.mulesoft.tools.migration.helper.DocumentHelper;
import org.jdom2.Element;
import org.junit.Test;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertNotNull;

public class MoveAttributesToChildNodeTest {

    private MoveAttributesToChildNode moveAttributesToChildNodeStep;

    private static final String EXAMPLE_FILE_PATH = "src/test/resources/mule/examples/http/http-all-use-case.xml";

    @Test
    public void testMoveAttributesToChildNode() throws Exception {
        moveAttributesToChildNodeStep = new MoveAttributesToChildNode("path;allowedMethods", "response-builder");
        DocumentHelper.getNodesFromFile("//http:listener", moveAttributesToChildNodeStep, EXAMPLE_FILE_PATH);
        moveAttributesToChildNodeStep.execute();
        Element node = moveAttributesToChildNodeStep.getNodes().get(0);
        assertNotNull(node.getChildren().get(0).getAttribute("path"));
    }

    @Test
    public void testBadChildNode() throws Exception {
        moveAttributesToChildNodeStep = new MoveAttributesToChildNode("host;protocol;port", "node");
        DocumentHelper.getNodesFromFile("//http:listener-config", moveAttributesToChildNodeStep, EXAMPLE_FILE_PATH);
        moveAttributesToChildNodeStep.execute();
        Element node = moveAttributesToChildNodeStep.getNodes().get(0);
        assertNull(node.getChildren().get(0).getAttribute("host"));
    }

    @Test
    public void testEmptyAttributes() throws Exception {
        moveAttributesToChildNodeStep = new MoveAttributesToChildNode("", "listener-connection");
        DocumentHelper.getNodesFromFile("//http:listener-config", moveAttributesToChildNodeStep, EXAMPLE_FILE_PATH);
        moveAttributesToChildNodeStep.execute();
        Element node = moveAttributesToChildNodeStep.getNodes().get(0);
        assertNull(node.getChildren().get(0).getAttribute("host"));
    }
}
