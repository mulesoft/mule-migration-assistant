/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.task.step;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.junit.Test;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getNodesFromFile;
import static org.junit.Assert.assertTrue;

public class AddAttributeTest {

    private AddAttribute attributeStep;

    private static final String EXAMPLE_FILE_PATH = "src/test/resources/munit/examples/simple.xml";

    @Test
    public void addAtributeOnNode() throws Exception {
        attributeStep = new AddAttribute("pepe", "lala");
        getNodesFromFile("//munit:assert-true", attributeStep, EXAMPLE_FILE_PATH);
        attributeStep.execute();
        assertTrue(attributeStep.getNodes().get(0).hasAttributes() == true );
    }

    @Test
    public void addAtributeOnNodeComplexValue() throws Exception {
        attributeStep = new AddAttribute("pepe", "#[payload().asString()]");
        getNodesFromFile("//munit:assert-true", attributeStep, EXAMPLE_FILE_PATH);
        attributeStep.execute();
        assertTrue(attributeStep.getNodes().get(0).hasAttributes() == true );
    }

    @Test
    public void addAtributeOnNodeAlreadyExistsOverwrites() throws Exception {
        attributeStep = new AddAttribute("pepe", "lala");
        getNodesFromFile("//munit:assert-true", attributeStep, EXAMPLE_FILE_PATH);
        attributeStep.execute();
        attributeStep.execute();
        assertTrue(attributeStep.getNodes().get(0).hasAttributes() == true );
    }

    @Test(expected = MigrationStepException.class)
    public void addNullAttribute() throws Exception {
        attributeStep = new AddAttribute(null, "lala");
        getNodesFromFile("//munit:assert-true",attributeStep,EXAMPLE_FILE_PATH);
        attributeStep.execute();
    }

    @Test
    public void addEmptyAttribute() throws Exception {
        attributeStep = new AddAttribute("ignore", "");
        getNodesFromFile("//munit:assert-true",attributeStep,EXAMPLE_FILE_PATH);
        attributeStep.execute();
        assertTrue(attributeStep.getNodes().get(0).hasAttributes() == true );
    }
}
