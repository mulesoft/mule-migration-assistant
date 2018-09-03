/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.helper;

import static java.util.Collections.emptyList;

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

//TODO This class is no longer valid, we can have something similar but to obtain an application model
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
    return getElementsFromDocument(doc, xPathExpression, "mule");
  }

  public static List<Element> getElementsFromDocuments(String xPathExpression, Document... docs) {
    List<Element> elements = new ArrayList<>();

    for (Document doc : docs) {
      elements.addAll(getElementsFromDocument(doc, xPathExpression));
    }

    return elements;
  }

  public static List<Element> getElementsFromDocument(Document doc, String xPathExpression, String defaultNamespacePrefix) {
    try {
      List<Namespace> namespaces = new ArrayList<>();
      namespaces.addAll(doc.getRootElement().getAdditionalNamespaces());

      if (namespaces.stream().noneMatch(n -> defaultNamespacePrefix.equals(n.getPrefix()))) {
        namespaces.add(Namespace.getNamespace(defaultNamespacePrefix, doc.getRootElement().getNamespace().getURI()));
      }

      if (namespaces.stream().anyMatch(n -> "".equals(n.getPrefix()))) {
        Namespace coreNs = namespaces.stream().filter(n -> "".equals(n.getPrefix())).findFirst().get();
        namespaces.remove(coreNs);
        namespaces.add(Namespace.getNamespace("mule", coreNs.getURI()));
      }

      XPathExpression<Element> xpath = XPathFactory.instance().compile(xPathExpression, Filters.element(), null, namespaces);
      List<Element> nodes = xpath.evaluate(doc);
      return nodes;
    } catch (IllegalArgumentException e) {
      if (e.getMessage().matches("Namespace with prefix '\\w+' has not been declared.")) {
        return emptyList();
      } else {
        throw e;
      }
    }
  }

  public static void getNodesFromFile(String Xpath, AbstractApplicationModelMigrationStep step, String filePath)
      throws Exception {
    Document document = getDocument(filePath);
    List<Element> nodes = getElementsFromDocument(document, Xpath);
    //    step.setDocument(document);
    //    step.setNodes(nodes);
  }
}
