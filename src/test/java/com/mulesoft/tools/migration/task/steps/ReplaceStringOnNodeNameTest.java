package com.mulesoft.tools.migration.task.steps;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.jdom2.Element;
import org.junit.Test;

import java.util.Collections;

import static com.mulesoft.tools.migration.helpers.DocumentHelpers.getNodesFromFile;
import static org.junit.Assert.*;

public class ReplaceStringOnNodeNameTest {

    private ReplaceStringOnNodeName replaceStringStep;

    private static final String EXAMPLE_FILE_PATH = "src/test/resources/munit/examples/sample-file.xml";

    @Test
    public void nodeNotContainStringToRemove() throws Exception {
        replaceStringStep = new ReplaceStringOnNodeName("when2", "pepe");
        getNodesFromFile("//mock:when", replaceStringStep, EXAMPLE_FILE_PATH);
        replaceStringStep.execute();
        Element node = replaceStringStep.getNodes().get(0);
        assertFalse(node.getName().equals("pepe"));
    }

    @Test (expected = MigrationStepException.class)
    public void replaceToNullString() throws Exception {
        replaceStringStep = new ReplaceStringOnNodeName("when", null);
        getNodesFromFile("//mock:when", replaceStringStep, EXAMPLE_FILE_PATH);
        replaceStringStep.execute();
    }

    @Test (expected = MigrationStepException.class)
    public void replaceToEmptyString() throws Exception {
        replaceStringStep = new ReplaceStringOnNodeName("when", "");
        getNodesFromFile("//mock:when", replaceStringStep, EXAMPLE_FILE_PATH);
        replaceStringStep.execute();
    }

    @Test
    public void replaceSimpleString() throws Exception {
        replaceStringStep = new ReplaceStringOnNodeName("true", "that");
        getNodesFromFile("//munit:assert-true", replaceStringStep, EXAMPLE_FILE_PATH);
        replaceStringStep.execute();
        Element node = replaceStringStep.getNodes().get(0);
        assertTrue(node.getName().equals("assert-that"));
    }

    @Test
    public void executeTaskOverEmptyNodeCollection() throws Exception {
        replaceStringStep = new ReplaceStringOnNodeName("true", "that");
        replaceStringStep.setNodes(Collections.<Element>emptyList());
        replaceStringStep.execute();
        assertEquals(Collections.<Element>emptyList(), replaceStringStep.getNodes());
    }

}
