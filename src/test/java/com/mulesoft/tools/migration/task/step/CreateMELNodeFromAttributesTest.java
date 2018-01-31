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

public class CreateMELNodeFromAttributesTest {

    private CreateMELNodeFromAttributes createMELNodeFromAttributesStep;

    private static final String EXAMPLE_FILE_PATH = "src/test/resources/mule/examples/http/http-all-use-case.xml";

    @Test
    public void testBadHeader() throws Exception {
        createMELNodeFromAttributesStep = new CreateMELNodeFromAttributes("header1", "headers",
                "http", "http://www.mulesoft.org/schema/mule/http",
                "headerName", "value");
        getNodesFromFile("//http:request-builder",createMELNodeFromAttributesStep,EXAMPLE_FILE_PATH);
        createMELNodeFromAttributesStep.execute();
        assertTrue(createMELNodeFromAttributesStep.getNodes().get(0).getChildren().size() == 8);
    }

    @Test
    public void testBadAttributes() throws Exception {
        createMELNodeFromAttributesStep = new CreateMELNodeFromAttributes("header", "headers",
                "http", "http://www.mulesoft.org/schema/mule/http",
                "headerNamexx", "valuexx");
        getNodesFromFile("//http:request-builder",createMELNodeFromAttributesStep,EXAMPLE_FILE_PATH);
        createMELNodeFromAttributesStep.execute();
        assertTrue(createMELNodeFromAttributesStep.getNodes().get(0).getChildren().size() == 8);
    }

    @Test
    public void testCreateMELNodeFromAttributes() throws Exception {
        createMELNodeFromAttributesStep = new CreateMELNodeFromAttributes("header", "headers",
                "http", "http://www.mulesoft.org/schema/mule/http",
                "headerName", "value");
        getNodesFromFile("//http:request-builder",createMELNodeFromAttributesStep,EXAMPLE_FILE_PATH);
        createMELNodeFromAttributesStep.execute();
        assertTrue(createMELNodeFromAttributesStep.getNodes().get(0).getChildren().size() == 7);
    }
}
