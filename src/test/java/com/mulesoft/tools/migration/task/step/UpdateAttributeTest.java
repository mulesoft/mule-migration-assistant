/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.task.step;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.helper.DocumentHelper;
import org.jdom2.Element;
import org.junit.Test;

import static org.junit.Assert.*;

public class UpdateAttributeTest {

    private UpdateAttribute updateAttribute;

    private static final String EXAMPLE_FILE_PATH = "src/test/resources/munit/examples/sample-file.xml";

    @Test
    public void updateValueToNonExistingAttribute() throws Exception {
        updateAttribute = new UpdateAttribute("condi", "lala");
        DocumentHelper.getNodesFromFile("//munit:assert-true", updateAttribute, EXAMPLE_FILE_PATH);
        updateAttribute.execute();
        Element node = updateAttribute.getNodes().get(0);
        assertNull(node.getAttribute("pepe"));
    }

    @Test
    public void updateSimpleValue() throws Exception {
        updateAttribute = new UpdateAttribute("condition", "lala");
        DocumentHelper.getNodesFromFile("//munit:assert-true", updateAttribute, EXAMPLE_FILE_PATH);
        updateAttribute.execute();
        Element node = updateAttribute.getNodes().get(0);
        assertEquals("lala", node.getAttributeValue("condition"));
    }

    @Test
    public void updateValueToComplex() throws Exception {
        updateAttribute = new UpdateAttribute("condition", "#[message.flowVars('pepe')]");
        DocumentHelper.getNodesFromFile("//munit:assert-true", updateAttribute, EXAMPLE_FILE_PATH);
        updateAttribute.execute();
        Element node = updateAttribute.getNodes().get(0);
        assertEquals("#[message.flowVars('pepe')]", node.getAttributeValue("condition"));
    }

    @Test
    public void updateValueToPlaceholder() throws Exception {
        updateAttribute = new UpdateAttribute("condition", "${myprop}");
        DocumentHelper.getNodesFromFile("//munit:assert-true", updateAttribute, EXAMPLE_FILE_PATH);
        updateAttribute.execute();
        Element node = updateAttribute.getNodes().get(0);
        assertEquals("${myprop}", node.getAttributeValue("condition"));
    }

    @Test (expected = MigrationStepException.class)
    public void updateValueToNull() throws Exception {
        updateAttribute = new UpdateAttribute("condition", null);
        DocumentHelper.getNodesFromFile("//munit:assert-true", updateAttribute, EXAMPLE_FILE_PATH);
        updateAttribute.execute();
        Element node = updateAttribute.getNodes().get(0);
    }

    @Test
    public void updateValueToEmpty() throws Exception {
        updateAttribute = new UpdateAttribute("condition", "");
        DocumentHelper.getNodesFromFile("//munit:assert-true", updateAttribute, EXAMPLE_FILE_PATH);
        updateAttribute.execute();
        Element node = updateAttribute.getNodes().get(0);
        assertEquals("", node.getAttributeValue("condition"));
    }
}
