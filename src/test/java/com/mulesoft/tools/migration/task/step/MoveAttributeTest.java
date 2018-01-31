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

public class MoveAttributeTest {

    private MoveAttribute moveAttribute;

    private static final String EXAMPLE_FILE_PATH = "src/test/resources/mule/examples/wsc/wsc-use-case.xml";

    @Test
    public void testMoveAttribute() throws Exception {
        moveAttribute = new MoveAttribute("mtomEnabled", "consumer-config", "ws", "http://www.mulesoft.org/schema/mule/ws",
                "config-ref", "name");
        DocumentHelper.getNodesFromFile("//ws:consumer", moveAttribute, EXAMPLE_FILE_PATH);
        moveAttribute.execute();
        Element node = moveAttribute.getNodes().get(0);
        assertNull(node.getAttribute("mtomEnabled"));
    }

    @Test
    public void testBadTargetNode() throws Exception {
        moveAttribute = new MoveAttribute("mtomEnabled", "consumer-config1", "ws", "http://www.mulesoft.org/schema/mule/ws",
                "config-ref", "name");
        DocumentHelper.getNodesFromFile("//ws:consumer", moveAttribute, EXAMPLE_FILE_PATH);
        moveAttribute.execute();
        Element node = moveAttribute.getNodes().get(0);
        assertNotNull(node.getAttribute("mtomEnabled"));
    }

    @Test
    public void testEmptyAttribute() throws Exception {
        moveAttribute = new MoveAttribute("", "consumer-config", "ws", "http://www.mulesoft.org/schema/mule/ws",
                "config-ref", "name");
        DocumentHelper.getNodesFromFile("//ws:consumer", moveAttribute, EXAMPLE_FILE_PATH);
        moveAttribute.execute();
        Element node = moveAttribute.getNodes().get(0);
        assertNotNull(node.getAttribute("mtomEnabled"));
    }
}
