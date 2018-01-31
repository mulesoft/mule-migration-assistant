/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.task.step;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.jdom2.Element;
import org.junit.Test;

import java.util.Collections;

import static com.mulesoft.tools.migration.helper.DocumentHelper.getNodesFromFile;
import static org.junit.Assert.*;

public class UpdateAttributeNameTest {

    private UpdateAttributeName updateAttributeName;

    private static final String EXAMPLE_FILE_PATH = "src/test/resources/munit/examples/sample-file.xml";

    @Test
    public void updateNameToNonExistingAttribute() throws Exception {
        updateAttributeName = new UpdateAttributeName("condi", "pepe");
        getNodesFromFile("//munit:assert-true", updateAttributeName, EXAMPLE_FILE_PATH);
        updateAttributeName.execute();
        Element node = updateAttributeName.getNodes().get(0);
        assertNull(node.getAttribute("pepe"));
    }

    @Test
    public void updateNameToAttribute() throws Exception {
        updateAttributeName = new UpdateAttributeName("condition", "pepe");
        getNodesFromFile("//munit:assert-true", updateAttributeName, EXAMPLE_FILE_PATH);
        updateAttributeName.execute();
        Element node = updateAttributeName.getNodes().get(0);
        assertNotNull(node.getAttribute("pepe"));
    }

    @Test
    public void updateNameToAttributeToAlreadyDeclaredOne() throws Exception {
        updateAttributeName = new UpdateAttributeName("message", "level");
        getNodesFromFile("//*[contains(local-name(),'logger')]", updateAttributeName, EXAMPLE_FILE_PATH);
        updateAttributeName.execute();
        Element node = updateAttributeName.getNodes().get(0);
        assertNotNull(node.getAttribute("level"));
    }

    @Test (expected = MigrationStepException.class)
    public void updateNameToEmptyString() throws Exception {
        updateAttributeName = new UpdateAttributeName("message", "");
        getNodesFromFile("//*[contains(local-name(),'logger')]", updateAttributeName, EXAMPLE_FILE_PATH);
        updateAttributeName.execute();
        Element node = updateAttributeName.getNodes().get(0);
    }

    @Test (expected = MigrationStepException.class)
    public void updateNameToNullString() throws Exception {
        updateAttributeName = new UpdateAttributeName("message", null);
        getNodesFromFile("//*[contains(local-name(),'logger')]", updateAttributeName, EXAMPLE_FILE_PATH);
        updateAttributeName.execute();
        Element node = updateAttributeName.getNodes().get(0);
    }

    @Test
    public void executeTaskToEmptyNodeList() throws Exception {
        updateAttributeName = new UpdateAttributeName("message", "test");
        getNodesFromFile("//lala", updateAttributeName, EXAMPLE_FILE_PATH);
        updateAttributeName.execute();
        assertEquals(Collections.<Element>emptyList(), updateAttributeName.getNodes());
    }
}
