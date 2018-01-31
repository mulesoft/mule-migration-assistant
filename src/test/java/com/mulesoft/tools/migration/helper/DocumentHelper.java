/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.helper;

import com.mulesoft.tools.migration.task.step.MigrationStep;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class DocumentHelper {

    public static Document getDocument(String path) throws Exception {
        SAXBuilder saxBuilder = new SAXBuilder();
        File file = new File(path);
        Document document = saxBuilder.build(file);
        return document;
    }

    public static void restoreTestDocument(Document doc, String path) throws Exception {
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        xmlOutputter.output(doc, new FileOutputStream(path));
    }

    public static List<Element> getElementsFromDocument(Document doc, String xPathExpression) {
        XPathExpression<Element> xpath = XPathFactory.instance().compile(xPathExpression, Filters.element(), null, doc.getRootElement().getAdditionalNamespaces());
        List<Element> nodes = xpath.evaluate(doc);
        return nodes;
    }

    public static void getNodesFromFile(String Xpath, MigrationStep step, String filePath) throws Exception {
        Document document = getDocument(filePath);
        List<Element> nodes = getElementsFromDocument(document, Xpath);
        step.setDocument(document);
        step.setNodes(nodes);
    }
}
