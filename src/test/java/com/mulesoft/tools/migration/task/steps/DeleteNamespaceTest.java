package com.mulesoft.tools.migration.task.steps;

import com.mulesoft.tools.migration.helpers.DocumentHelpers;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class DeleteNamespaceTest {

    private DeleteNamespace deleteNamespaceStep;

    private static final String EXAMPLE_FILE_PATH = "src/test/resources/mule/examples/http-all-use-case.xml";

    @Test
    public void testBadNamespace() throws Exception {
        deleteNamespaceStep = new DeleteNamespace("a","http://www.mulesoft.org/schema/mule/http",
                "http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd");
        DocumentHelpers.GetNodesFromFile("//mule",deleteNamespaceStep,EXAMPLE_FILE_PATH );
        deleteNamespaceStep.execute();
        assertTrue(deleteNamespaceStep.getDocument().getRootElement().getNamespace("http") != null);
    }

    @Test
    public void testBadNamespaceUri() throws Exception {
        deleteNamespaceStep = new DeleteNamespace("http","b",
                "http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd");
        DocumentHelpers.GetNodesFromFile("//mule",deleteNamespaceStep,EXAMPLE_FILE_PATH );
        deleteNamespaceStep.execute();
        assertTrue(deleteNamespaceStep.getDocument().getRootElement().getNamespace("http") != null);
    }

    @Test
    public void testBadSchemaLocationUrl() throws Exception {
        deleteNamespaceStep = new DeleteNamespace("http","http://www.mulesoft.org/schema/mule/http", "c");
        DocumentHelpers.GetNodesFromFile("//mule",deleteNamespaceStep,EXAMPLE_FILE_PATH );
        deleteNamespaceStep.execute();
        assertTrue(deleteNamespaceStep.getDocument().getRootElement().getNamespace("http") != null);
    }

    @Test
    public void testBlankValues() throws Exception {
        deleteNamespaceStep = new DeleteNamespace("","", "");
        DocumentHelpers.GetNodesFromFile("//mule",deleteNamespaceStep,EXAMPLE_FILE_PATH );
        deleteNamespaceStep.execute();
        assertTrue(deleteNamespaceStep.getDocument().getRootElement().getNamespace("http") != null);
    }

    @Test
    public void testDeleteNamespace() throws Exception {
        deleteNamespaceStep = new DeleteNamespace("http","http://www.mulesoft.org/schema/mule/http",
                "http://www.mulesoft.org/schema/mule/http/current/mule-http.xsd");
        DocumentHelpers.GetNodesFromFile("//mule",deleteNamespaceStep,EXAMPLE_FILE_PATH );
        deleteNamespaceStep.execute();
        assertTrue(deleteNamespaceStep.getDocument().getRootElement().getNamespace("http") == null);
    }
}
