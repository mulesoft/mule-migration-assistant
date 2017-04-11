package com.mulesoft.tools.migration.task.steps;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.junit.Test;

import static com.mulesoft.tools.migration.helpers.DocumentHelpers.getNodesFromFile;
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
