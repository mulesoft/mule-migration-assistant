package com.mulesoft.tools.migration.task.steps;

import com.mulesoft.tools.migration.helpers.DocumentHelpers;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CreateMELNodeFromAttributesTest {

    private CreateMELNodeFromAttributes createMELNodeFromAttributesStep;

    private static final String EXAMPLE_FILE_PATH = "src/test/resources/mule/examples/http-all-use-case.xml";

    @Test
    public void testBadHeader() throws Exception {
        createMELNodeFromAttributesStep = new CreateMELNodeFromAttributes("header1", "headers",
                "http", "http://www.mulesoft.org/schema/mule/http",
                "headerName", "value");
        DocumentHelpers.GetNodesFromFile("//http:request-builder",createMELNodeFromAttributesStep,EXAMPLE_FILE_PATH);
        createMELNodeFromAttributesStep.execute();
        assertTrue(createMELNodeFromAttributesStep.getNodes().get(0).getChildren().size() == 8);
    }

    @Test
    public void testBadAttributes() throws Exception {
        createMELNodeFromAttributesStep = new CreateMELNodeFromAttributes("header", "headers",
                "http", "http://www.mulesoft.org/schema/mule/http",
                "headerNamexx", "valuexx");
        DocumentHelpers.GetNodesFromFile("//http:request-builder",createMELNodeFromAttributesStep,EXAMPLE_FILE_PATH);
        createMELNodeFromAttributesStep.execute();
        assertTrue(createMELNodeFromAttributesStep.getNodes().get(0).getChildren().size() == 8);
    }

    @Test
    public void testHappyPath() throws Exception {
        createMELNodeFromAttributesStep = new CreateMELNodeFromAttributes("header", "headers",
                "http", "http://www.mulesoft.org/schema/mule/http",
                "headerName", "value");
        DocumentHelpers.GetNodesFromFile("//http:request-builder",createMELNodeFromAttributesStep,EXAMPLE_FILE_PATH);
        createMELNodeFromAttributesStep.execute();
        assertTrue(createMELNodeFromAttributesStep.getNodes().get(0).getChildren().size() == 7);
    }
}
