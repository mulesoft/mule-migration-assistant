/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.tasks;

import static java.util.Collections.emptyList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

public class DocumentHelper {

  public static Document getDocument(String path) throws Exception {
    SAXBuilder saxBuilder = new SAXBuilder();
    File file = new File(path);
    return saxBuilder.build(file);
  }

  public static List<Element> getElementsFromDocument(Document doc, String xPathExpression) {
    try {
      return getElementsFromDocument(doc, xPathExpression, "mule");
    } catch (IllegalArgumentException e) {
      if (e.getMessage().matches("Namespace with prefix '\\w+' has not been declared.")) {
        return emptyList();
      } else {
        throw e;
      }
    }
  }

  public static List<Element> getElementsFromDocument(Document doc, String xPathExpression, String defaultNamespacePrefix) {
    List<Namespace> namespaces = new ArrayList<>(doc.getRootElement().getAdditionalNamespaces());

    if (namespaces.stream().noneMatch(n -> defaultNamespacePrefix.equals(n.getPrefix()))) {
      namespaces.add(Namespace.getNamespace(defaultNamespacePrefix, doc.getRootElement().getNamespace().getURI()));
    }

    XPathExpression<Element> xpath = XPathFactory.instance().compile(xPathExpression, Filters.element(), null, namespaces);
    return xpath.evaluate(doc);
  }
}
