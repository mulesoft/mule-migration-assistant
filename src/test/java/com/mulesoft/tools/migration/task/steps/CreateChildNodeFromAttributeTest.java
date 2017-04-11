package com.mulesoft.tools.migration.task.steps;

import com.mulesoft.tools.migration.helpers.DocumentHelpers;
import org.junit.Test;

import static com.mulesoft.tools.migration.helpers.DocumentHelpers.getNodesFromFile;
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
