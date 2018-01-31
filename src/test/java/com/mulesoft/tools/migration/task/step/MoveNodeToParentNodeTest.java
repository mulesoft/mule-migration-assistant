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

public class MoveNodeToParentNodeTest {

    private MoveNodeToParentNode moveNodeToParentNodeStep;

    private static final String EXAMPLE_FILE_PATH = "src/test/resources/mule/examples/http/http-all-use-case.xml";

    @Test
    public void testMoveNodeToParentNode() throws Exception {
        moveNodeToParentNodeStep = new MoveNodeToParentNode("query-param", "http",
                "http://www.mulesoft.org/schema/mule/http");
        getNodesFromFile("//http:request-builder", moveNodeToParentNodeStep, EXAMPLE_FILE_PATH);
        moveNodeToParentNodeStep.execute();

        Element node = moveNodeToParentNodeStep.getNodes().get(0).getParentElement();
        int counter = 0;
        for (Element child : node.getChildren()) {
            if (child.getName().equals("query-param")) {
                counter ++;
            }
        }
        Assert.assertTrue(counter > 0);
    }

    @Test
    public void testBadSource() throws Exception {
        moveNodeToParentNodeStep = new MoveNodeToParentNode("query-param1", "http",
                "http://www.mulesoft.org/schema/mule/http");
        getNodesFromFile("//http:request-builder", moveNodeToParentNodeStep, EXAMPLE_FILE_PATH);
        moveNodeToParentNodeStep.execute();
        Element node = moveNodeToParentNodeStep.getNodes().get(0).getParentElement();
        Assert.assertTrue(node.getChildren().size() == 2);
    }
}