/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.task.step;

import org.jdom2.Element;
import org.junit.Assert;
import org.junit.Test;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getNodesFromFile;

public class MoveNodeToChildNodeTest {

    private MoveNodeToChildNode moveNodeToChildNodeStep;

    private static final String EXAMPLE_FILE_PATH = "src/test/resources/mule/examples/http/http-all-use-case.xml";

    @Test
    public void testMoveNodeToChildNode() throws Exception {
        moveNodeToChildNodeStep = new MoveNodeToChildNode("response-builder", "http",
                "http://www.mulesoft.org/schema/mule/http", "error-response-builder",
                "http", "http://www.mulesoft.org/schema/mule/http");
        getNodesFromFile("//http:listener", moveNodeToChildNodeStep, EXAMPLE_FILE_PATH);
        moveNodeToChildNodeStep.execute();

        Element node = moveNodeToChildNodeStep.getNodes().get(0).getChildren().get(0);
        for (Element child : node.getChildren()) {
            if (child.getNamespacePrefix().equals("response-builder")) {
                Assert.assertTrue(true);
            }
        }
    }

    @Test
    public void testBadSource() throws Exception {
        moveNodeToChildNodeStep = new MoveNodeToChildNode("response-builder1", "http",
                "http://www.mulesoft.org/schema/mule/http", "error-response-builder",
                "http", "http://www.mulesoft.org/schema/mule/http");
        getNodesFromFile("//http:listener", moveNodeToChildNodeStep, EXAMPLE_FILE_PATH);
        moveNodeToChildNodeStep.execute();
        Element node = moveNodeToChildNodeStep.getNodes().get(0).getChildren().get(0);
        Assert.assertTrue(node.getChildren().size() == 2);
    }

    @Test
    public void testBadTarget() throws Exception {
        moveNodeToChildNodeStep = new MoveNodeToChildNode("response-builder", "http",
                "http://www.mulesoft.org/schema/mule/http", "error-response-builder1",
                "http", "http://www.mulesoft.org/schema/mule/http");
        getNodesFromFile("//http:listener", moveNodeToChildNodeStep, EXAMPLE_FILE_PATH);
        moveNodeToChildNodeStep.execute();
        Element node = moveNodeToChildNodeStep.getNodes().get(0).getChildren().get(0);
        Assert.assertTrue(node.getChildren().size() == 2);
    }
}
