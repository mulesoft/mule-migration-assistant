/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.task.step;

import org.junit.Test;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getNodesFromFile;
import static org.junit.Assert.assertTrue;

public class MoveAttributeToMELContentTest {

    private MoveAttributeToMELContent moveAttributeToMELContentStep;

    private static final String EXAMPLE_FILE_PATH = "src/test/resources/mule/examples/http/http-all-use-case.xml";

    @Test
    public void testMoveAttributeToMELContent() throws Exception {
        moveAttributeToMELContentStep = new MoveAttributeToMELContent("expression");
        getNodesFromFile("//http:uri-params",moveAttributeToMELContentStep,EXAMPLE_FILE_PATH);
        moveAttributeToMELContentStep.execute();
        assertTrue(moveAttributeToMELContentStep.getNodes().get(0).getText().contains("mel"));
    }

    @Test
    public void testBadAttribute() throws Exception {
        moveAttributeToMELContentStep = new MoveAttributeToMELContent("expression1");
        getNodesFromFile("//http:uri-params",moveAttributeToMELContentStep,EXAMPLE_FILE_PATH);
        moveAttributeToMELContentStep.execute();
        assertTrue(!moveAttributeToMELContentStep.getNodes().get(0).getText().contains("mel"));
    }
}
