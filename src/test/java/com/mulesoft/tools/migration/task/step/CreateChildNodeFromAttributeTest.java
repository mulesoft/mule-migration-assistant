/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.task.step;

import org.junit.Test;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getNodesFromFile;
import static org.junit.Assert.*;

public class CreateChildNodeFromAttributeTest {

    private CreateChildNodeFromAttribute createAttNodeStep;

    private static final String EXAMPLE_FILE_PATH = "src/test/resources/munit/examples/simple.xml";
    @Test
    public void setAttributeNotExists() throws Exception {
        createAttNodeStep = new CreateChildNodeFromAttribute("lala");
        getNodesFromFile("//munit:assert-true", createAttNodeStep, EXAMPLE_FILE_PATH);
        createAttNodeStep.execute();
        assertTrue(createAttNodeStep.getNodes().get(0).getChildren().size() == 0);
    }

    @Test
    public void createWithEmptyAttributeValue() throws Exception {
        createAttNodeStep = new CreateChildNodeFromAttribute("");
        getNodesFromFile("//munit:assert-true", createAttNodeStep, EXAMPLE_FILE_PATH);
        createAttNodeStep.execute();
        assertTrue(createAttNodeStep.getNodes().get(0).getChildren().size() == 0);
    }

    @Test
    public void createSimpleNodeFromAttribute() throws Exception {
        createAttNodeStep = new CreateChildNodeFromAttribute("prop");
        getNodesFromFile("//munit:assert-true", createAttNodeStep, EXAMPLE_FILE_PATH);
        createAttNodeStep.execute();
        assertTrue(createAttNodeStep.getNodes().get(0).getChildren().size() == 1);
    }

}
