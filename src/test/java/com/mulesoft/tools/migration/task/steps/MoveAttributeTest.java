package com.mulesoft.tools.migration.task.steps;

import com.mulesoft.tools.migration.helpers.DocumentHelpers;
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
        DocumentHelpers.getNodesFromFile("//ws:consumer", moveAttribute, EXAMPLE_FILE_PATH);
        moveAttribute.execute();
        Element node = moveAttribute.getNodes().get(0);
        assertNull(node.getAttribute("mtomEnabled"));
    }

    @Test
    public void testBadTargetNode() throws Exception {
        moveAttribute = new MoveAttribute("mtomEnabled", "consumer-config1", "ws", "http://www.mulesoft.org/schema/mule/ws",
                "config-ref", "name");
        DocumentHelpers.getNodesFromFile("//ws:consumer", moveAttribute, EXAMPLE_FILE_PATH);
        moveAttribute.execute();
        Element node = moveAttribute.getNodes().get(0);
        assertNotNull(node.getAttribute("mtomEnabled"));
    }

    @Test
    public void testEmptyAttribute() throws Exception {
        moveAttribute = new MoveAttribute("", "consumer-config", "ws", "http://www.mulesoft.org/schema/mule/ws",
                "config-ref", "name");
        DocumentHelpers.getNodesFromFile("//ws:consumer", moveAttribute, EXAMPLE_FILE_PATH);
        moveAttribute.execute();
        Element node = moveAttribute.getNodes().get(0);
        assertNotNull(node.getAttribute("mtomEnabled"));
    }
}
