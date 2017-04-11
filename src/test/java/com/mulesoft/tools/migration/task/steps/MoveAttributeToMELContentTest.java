package com.mulesoft.tools.migration.task.steps;

import com.mulesoft.tools.migration.helpers.DocumentHelpers;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class MoveAttributeToMELContentTest {

    private MoveAttributeToMELContent moveAttributeToMELContentStep;

    private static final String EXAMPLE_FILE_PATH = "src/test/resources/mule/examples/http-all-use-case.xml";

    @Test
    public void testMoveAttributeToMELContent() throws Exception {
        moveAttributeToMELContentStep = new MoveAttributeToMELContent("expression");
        DocumentHelpers.GetNodesFromFile("//http:uri-params",moveAttributeToMELContentStep,EXAMPLE_FILE_PATH);
        moveAttributeToMELContentStep.execute();
        assertTrue(moveAttributeToMELContentStep.getNodes().get(0).getText().contains("mel"));
    }

    @Test
    public void testBadAttribute() throws Exception {
        moveAttributeToMELContentStep = new MoveAttributeToMELContent("expression1");
        DocumentHelpers.GetNodesFromFile("//http:uri-params",moveAttributeToMELContentStep,EXAMPLE_FILE_PATH);
        moveAttributeToMELContentStep.execute();
        assertTrue(!moveAttributeToMELContentStep.getNodes().get(0).getText().contains("mel"));
    }
}
