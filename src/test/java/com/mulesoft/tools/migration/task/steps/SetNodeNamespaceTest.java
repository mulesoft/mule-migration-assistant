package com.mulesoft.tools.migration.task.steps;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.jdom2.Document;
import org.junit.Test;

import static com.mulesoft.tools.migration.helpers.DocumentHelpers.getNodesFromFile;
import static org.junit.Assert.*;

public class SetNodeNamespaceTest {

    private SetNodeNamespace addNamespaceStep;

    private static final String EXAMPLE_FILE_PATH = "src/test/resources/munit/examples/sample-file.xml";

    @Test
    public void addNamespace() throws Exception {
        addNamespaceStep = new SetNodeNamespace("lala","http://local","http://lala.xsd");
        getNodesFromFile("/", addNamespaceStep, EXAMPLE_FILE_PATH);
        addNamespaceStep.execute();
        assertTrue(addNamespaceStep.getDocument().getRootElement().getAdditionalNamespaces().size() == 8);
    }

    @Test
    public void addNamespaceCheckSchemaLocationIsAdded() throws Exception {
        addNamespaceStep = new SetNodeNamespace("lala","http://local","http://lala.xsd");
        getNodesFromFile("/", addNamespaceStep, EXAMPLE_FILE_PATH);
        addNamespaceStep.execute();
        assertTrue(addNamespaceStep.getDocument().getRootElement().getAttributes().get(0).getValue().contains("lala.xsd"));
    }

    @Test
    public void addDuplicateNamespace() throws Exception {
        addNamespaceStep = new SetNodeNamespace("munit","http://www.mulesoft.org/schema/mule/munit","http://www.mulesoft.org/schema/mule/munit/current/mule-munit.xsd");
        getNodesFromFile("/", addNamespaceStep, EXAMPLE_FILE_PATH);
        addNamespaceStep.execute();
        assertTrue(addNamespaceStep.getDocument().getRootElement().getAttributes().get(0).getValue().contains("munit.xsd"));
    }

    @Test
    public void addNameSpaceEmptySchemaUrl() throws Exception {
        addNamespaceStep = new SetNodeNamespace("test", "http://localhost", null);
        getNodesFromFile("/", addNamespaceStep, EXAMPLE_FILE_PATH);
        addNamespaceStep.execute();
        assertTrue(addNamespaceStep.getDocument().getRootElement().getAttributes().get(0).getValue().contains("http://localhost"));
    }

    @Test (expected = MigrationStepException.class)
    public void addNameSpaceEmptyUrl() throws Exception {
        addNamespaceStep = new SetNodeNamespace("test", null, "http://localhost/m.xsd");
        getNodesFromFile("/", addNamespaceStep, EXAMPLE_FILE_PATH);
        addNamespaceStep.execute();
        assertTrue(addNamespaceStep.getDocument().getRootElement().getAttributes().get(0).getValue().contains("http://localhost/m.xsd"));
    }

    @Test
    public void addNameSpaceEmptyNamespace() throws Exception {
        addNamespaceStep = new SetNodeNamespace("test", "htp://localhost", "http://localhost/m.xsd");
        getNodesFromFile("/", addNamespaceStep, EXAMPLE_FILE_PATH);
        addNamespaceStep.execute();
        Document doc = addNamespaceStep.getDocument();
        assertTrue(addNamespaceStep.getDocument().getRootElement().getAttributes().get(0).getValue().contains("http://localhost/m.xsd"));
    }
}
