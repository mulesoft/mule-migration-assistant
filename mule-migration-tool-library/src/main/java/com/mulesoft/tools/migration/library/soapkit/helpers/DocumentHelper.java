/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.soapkit.helpers;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.Collections.emptyList;

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
    List<Namespace> namespaces = new ArrayList<>();
    namespaces.addAll(doc.getRootElement().getAdditionalNamespaces());

    if (namespaces.stream().noneMatch(n -> defaultNamespacePrefix.equals(n.getPrefix()))) {
      namespaces.add(Namespace.getNamespace(defaultNamespacePrefix, doc.getRootElement().getNamespace().getURI()));
    }

    XPathExpression<Element> xpath = XPathFactory.instance().compile(xPathExpression, Filters.element(), null, namespaces);
    List<Element> nodes = xpath.evaluate(doc);
    return nodes;
  }

  public static void renameAttribute(Element element, String attributeName, String newName) {
    final Attribute attribute = element.getAttribute(attributeName);
    if (attribute != null) {
      element.setAttribute(newName, attribute.getValue());
      element.removeAttribute(attribute);
    }
  }

  public static void replaceAttributeValue(Element element, String attributeName, Function<String, String> f) {
    final Attribute attribute = element.getAttribute(attributeName);
    if (attribute != null) {
      String value = attribute.getValue();
      value = f.apply(value);
      attribute.setValue(value);
    }
  }

  public static void replaceSlashesByBackSlashes(Element element, String attributeName) {
    replaceAttributeValue(element, attributeName, value -> value.replaceAll("/", "\\\\"));
  }

  public static void addElement(Element element, String name, String text) {
    final Namespace namespace = element.getNamespace();
    final Element child = new Element(name, namespace);
    child.setText(text);
    element.addContent(child);
  }
}
