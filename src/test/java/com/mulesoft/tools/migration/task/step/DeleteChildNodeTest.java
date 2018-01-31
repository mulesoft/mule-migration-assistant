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

public class DeleteChildNodeTest {

    private DeleteChildNode deleteChildNodeStep;

    private static final String EXAMPLE_FILE_PATH = "src/test/resources/mule/examples/http/http-all-use-case.xml";

    @Test
    public void testMoveNodeToParentNode() throws Exception {
        deleteChildNodeStep = new DeleteChildNode("request-builder", "http",
                "http://www.mulesoft.org/schema/mule/http");
        getNodesFromFile("//http:request", deleteChildNodeStep, EXAMPLE_FILE_PATH);
        deleteChildNodeStep.execute();
        Element node = deleteChildNodeStep.getNodes().get(0);
        Assert.assertTrue(node.getChildren().size() == 1);
    }

    @Test
    public void testBadSource() throws Exception {
        deleteChildNodeStep = new DeleteChildNode("request-builder1", "http",
                "http://www.mulesoft.org/schema/mule/http");
        getNodesFromFile("//http:request", deleteChildNodeStep, EXAMPLE_FILE_PATH);
        deleteChildNodeStep.execute();
        Element node = deleteChildNodeStep.getNodes().get(0);
        Assert.assertTrue(node.getChildren().size() == 2);
    }
}