package com.mulesoft.tools.migration.task.steps;

import org.jdom2.Element;
import org.junit.Test;

import static com.mulesoft.tools.migration.helpers.DocumentHelpers.getNodesFromFile;
import static org.junit.Assert.*;

public class MoveAttributeToChildNodeTest {

    private MoveAttributeToChildNode moveAttStep;

    private static final String EXAMPLE_FILE_PATH = "src/test/resources/munit/examples/sample-file.xml";

    @Test
    public void moveToChildNode() throws Exception {
        moveAttStep = new MoveAttributeToChildNode("messageProcessor", "with-attributes");
        getNodesFromFile("//mock:when", moveAttStep, EXAMPLE_FILE_PATH);
        moveAttStep.execute();
        Element node = moveAttStep.getNodes().get(0);
        assertNotNull(node.getChildren().get(0).getAttribute("messageProcessor"));
    }

    @Test
    public void moveAttributeToNotDefinedChildNode() throws Exception {
        moveAttStep = new MoveAttributeToChildNode("messageProcessor", "pepe");
        getNodesFromFile("//mock:when", moveAttStep, EXAMPLE_FILE_PATH);
        moveAttStep.execute();
        Element node = moveAttStep.getNodes().get(0);
        assertNull(node.getChild("pepe"));
    }

    @Test
    public void moveAttributeNotExistsOnNode() throws Exception {
        moveAttStep = new MoveAttributeToChildNode("pepe", "with-attributes");
        getNodesFromFile("//mock:when", moveAttStep, EXAMPLE_FILE_PATH);
        moveAttStep.execute();
        Element node = moveAttStep.getNodes().get(0);
        assertNull(node.getChildren().get(0).getAttribute("pepe"));
    }
}
