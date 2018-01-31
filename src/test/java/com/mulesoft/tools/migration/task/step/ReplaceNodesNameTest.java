/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.task.step;

import org.jdom2.Element;
import org.junit.Test;

import java.util.Collections;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getNodesFromFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReplaceNodesNameTest {

    private ReplaceNodesName replaceQName;

    private static final String EXAMPLE_FILE_PATH = "src/test/resources/munit/examples/sample-file.xml";

    @Test
    public void replaceQNameTestNodes() throws Exception {
        replaceQName = new ReplaceNodesName("munit", "test2");
        getNodesFromFile("//munit:test", replaceQName, EXAMPLE_FILE_PATH);
        replaceQName.execute();
        String newName = replaceQName.getNodes().get(0).getName();
        assertEquals("test2", newName);
    }

    @Test
    public void replaceQNameSubChildNodes() throws Exception {
        replaceQName = new ReplaceNodesName("mock", "mock");
        getNodesFromFile("//mock:when", replaceQName, EXAMPLE_FILE_PATH);
        replaceQName.execute();
        String newName = replaceQName.getNodes().get(0).getName();
        assertEquals("mock", newName);
    }

    @Test
    public void replaceQNameNotFoundNameSpace() throws Exception {
        replaceQName = new ReplaceNodesName("lalero", "test");
        getNodesFromFile("//mock:when", replaceQName, EXAMPLE_FILE_PATH);
        replaceQName.execute();
        String newName = replaceQName.getNodes().get(0).getName();
        assertEquals("when", newName);
    }

    @Test
    public void replaceQNameEmptyNodes() throws Exception {
        replaceQName = new ReplaceNodesName("munit", "lala");
        getNodesFromFile("//mock:when2423", replaceQName, EXAMPLE_FILE_PATH);
        replaceQName.execute();
        assertTrue(replaceQName.getNodes()== Collections.<Element>emptyList());
    }
}
