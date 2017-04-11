package com.mulesoft.tools.migration.task.steps;

import org.jdom2.Element;
import org.junit.Test;

import java.util.List;

import static com.mulesoft.tools.migration.helpers.DocumentHelpers.getNodesFromFile;
import static org.junit.Assert.*;

public class NegateAttributeValueTest {

    private NegateAttributeValue negateAtt;

    private static final String EXAMPLE_FILE_PATH = "src/test/resources/munit/examples/simple.xml";

    @Test
    public void notFoundAttribute() throws Exception {
        negateAtt = new NegateAttributeValue("lala");
        getNodesFromFile("//munit:assert-true", negateAtt, EXAMPLE_FILE_PATH);
        negateAtt.execute();
        List<Element> nodes = negateAtt.getNodes();
        assertTrue(nodes.get(0).getAttribute("lala") == null);
    }

    @Test
    public void nullAttribute() throws Exception {
        negateAtt = new NegateAttributeValue(null);
        getNodesFromFile("//munit:assert-true", negateAtt, EXAMPLE_FILE_PATH);
        negateAtt.execute();
        assertTrue(negateAtt.getNodes() != null);
    }

    @Test
    public void negateSimpleAttribute() throws Exception {
        negateAtt = new NegateAttributeValue("message");
        getNodesFromFile("//munit:assert-true", negateAtt, EXAMPLE_FILE_PATH);
        negateAtt.execute();
        List<Element> nodes = negateAtt.getNodes();
        assertEquals(nodes.get(0).getAttributeValue("message"), "#[not(this is sample)]");
    }

    @Test
    public void negateAttributeInsideQuotes() throws Exception {
        negateAtt = new NegateAttributeValue("condition");
        getNodesFromFile("//munit:assert-true", negateAtt, EXAMPLE_FILE_PATH);
        negateAtt.execute();
        List<Element> nodes = negateAtt.getNodes();
        assertEquals( "#[not('the new payload'.equals(payload))]", nodes.get(0).getAttributeValue("condition"));
    }

    @Test
    public void negatePlaceHolderAttribute() throws Exception {
        negateAtt = new NegateAttributeValue("prop");
        getNodesFromFile("//munit:assert-true", negateAtt, EXAMPLE_FILE_PATH);
        negateAtt.execute();
        List<Element> nodes = negateAtt.getNodes();
        assertEquals("#[not(${lala})]", nodes.get(0).getAttributeValue("prop"));
    }
}
