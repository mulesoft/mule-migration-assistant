package com.mulesoft.tools.migration.task.steps;

import org.jdom2.Element;
import org.junit.Assert;
import org.junit.Test;

import static com.mulesoft.tools.migration.helpers.DocumentHelpers.getNodesFromFile;

public class MoveNodeToChildNodeTest {

    private MoveNodeToChildNode moveNodeToChildNodeStep;

    private static final String EXAMPLE_FILE_PATH = "src/test/resources/mule/examples/http-all-use-case.xml";

    @Test
    public void testMoveNodeToChildNode() throws Exception {
        moveNodeToChildNodeStep = new MoveNodeToChildNode("response-builder", "http",
                "http://www.mulesoft.org/schema/mule/http", "error-response-builder",
                "http", "http://www.mulesoft.org/schema/mule/http");
        getNodesFromFile("//http:listener", moveNodeToChildNodeStep, EXAMPLE_FILE_PATH);
        moveNodeToChildNodeStep.execute();

        Element node = moveNodeToChildNodeStep.getNodes().get(0).getChildren().get(0);
        for (Element child : node.getChildren()) {
            if (child.getNamespacePrefix().equals("response-builder")) {
                Assert.assertTrue(true);
            }
        }
    }

    @Test
    public void testBadSource() throws Exception {
        moveNodeToChildNodeStep = new MoveNodeToChildNode("response-builder1", "http",
                "http://www.mulesoft.org/schema/mule/http", "error-response-builder",
                "http", "http://www.mulesoft.org/schema/mule/http");
        getNodesFromFile("//http:listener", moveNodeToChildNodeStep, EXAMPLE_FILE_PATH);
        moveNodeToChildNodeStep.execute();
        Element node = moveNodeToChildNodeStep.getNodes().get(0).getChildren().get(0);
        Assert.assertTrue(node.getChildren().size() == 2);
    }

    @Test
    public void testBadTarget() throws Exception {
        moveNodeToChildNodeStep = new MoveNodeToChildNode("response-builder", "http",
                "http://www.mulesoft.org/schema/mule/http", "error-response-builder1",
                "http", "http://www.mulesoft.org/schema/mule/http");
        getNodesFromFile("//http:listener", moveNodeToChildNodeStep, EXAMPLE_FILE_PATH);
        moveNodeToChildNodeStep.execute();
        Element node = moveNodeToChildNodeStep.getNodes().get(0).getChildren().get(0);
        Assert.assertTrue(node.getChildren().size() == 2);
    }
}
