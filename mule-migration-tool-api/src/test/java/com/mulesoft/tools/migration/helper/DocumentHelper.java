/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.helper;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to work with JDOM Documents
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
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
    List<Namespace> namespaces = new ArrayList<>();
    namespaces.add(Namespace.getNamespace("mule", doc.getRootElement().getNamespace().getURI()));
    namespaces.addAll(doc.getRootElement().getAdditionalNamespaces());

    XPathExpression<Element> xpath = XPathFactory.instance().compile(xPathExpression, Filters.element(), null, namespaces);
    List<Element> nodes = xpath.evaluate(doc);
    return nodes;
  }

  public static void getNodesFromFile(String Xpath, AbstractApplicationModelMigrationStep step, String filePath)
      throws Exception {
    Document document = getDocument(filePath);
    List<Element> nodes = getElementsFromDocument(document, Xpath);
    //    step.setDocument(document);
    //    step.setNodes(nodes);
  }
}
