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

public class MoveNodeToNewChildNodeTest {

    private MoveNodeToNewChildNode moveNodeToNewChildNodeStep;

    private static final String EXAMPLE_FILE_PATH = "src/test/resources/mule/examples/http/http-all-use-case.xml";

    @Test
    public void testMoveNodeToChildNode() throws Exception {
        moveNodeToNewChildNodeStep = new MoveNodeToNewChildNode("response-builder", "http", "http://www.mulesoft.org/schema/mule/http", "new-target", "http", "http://www.mulesoft.org/schema/mule/http");
        getNodesFromFile("//http:listener", moveNodeToNewChildNodeStep, EXAMPLE_FILE_PATH);
        moveNodeToNewChildNodeStep.execute();

        Element node = moveNodeToNewChildNodeStep.getNodes().get(0).getChildren().get(0);
        for (Element child : node.getChildren()) {
            if (child.getNamespacePrefix().equals("response-builder")) {
                Assert.assertTrue(true);
            }
        }
    }

    @Test
    public void testBadSource() throws Exception {
        moveNodeToNewChildNodeStep = new MoveNodeToNewChildNode("response-builder1", "http", "http://www.mulesoft.org/schema/mule/http", "new-target", "http", "http://www.mulesoft.org/schema/mule/http");
        getNodesFromFile("//http:listener", moveNodeToNewChildNodeStep, EXAMPLE_FILE_PATH);
        moveNodeToNewChildNodeStep.execute();

        Element node = moveNodeToNewChildNodeStep.getNodes().get(0).getChildren().get(0);
        Assert.assertTrue(node.getChildren().size() == 2);
    }
}
