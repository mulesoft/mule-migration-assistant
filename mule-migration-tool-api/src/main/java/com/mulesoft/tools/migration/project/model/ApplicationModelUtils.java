/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.project.model;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Utilities to work with DOM
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ApplicationModelUtils {

  public static Function<Element, Element> changeNodeName(String nameSpace, String name) {
    Function<Element, Element> f = e -> {
      e.setNamespace(e.getDocument().getRootElement().getNamespace(nameSpace));
      e.setName(name);
      // TODO missing reporting ReplaceNodesName.execute()
      return e;
    };

    return f;
  }

  public static Function<Element, Element> addAttribute(String name, String value) {
    Function<Element, Element> f = e -> {
      if (e != null) {
        Attribute att = new Attribute(name, value);
        e.setAttribute(att);
        // TODO missing reporting AddAttribute.execute
      }
      return e;
    };
    return f;
  }

  public static Function<Element, Element> changeAttribute(String name, Optional<String> newName, Optional<String> value) {
    Function<Element, Element> f = e -> {
      Attribute attribute = e.getAttribute(name);
      if (attribute != null) {
        newName.ifPresent(nn -> attribute.setName(nn));
        value.ifPresent(v -> attribute.setValue(v));
        // TODO missing reporting UpdateAttributeName.execute
      }
      return e;
    };
    return f;
  }

  public static Function<Element, Element> removeAttribute(String name) {
    Function<Element, Element> f = e -> {
      Attribute attribute = e.getAttribute(name);
      if (attribute != null) {
        e.removeAttribute(name);
        // TODO missing reporting
      }
      return e;
    };
    return f;
  }

  public static Function<Element, Element> moveContentToChild(String childNodeName) {
    Function<Element, Element> f = e -> {
      if (e != null) {
        List<Element> childElements =
            e.getChildren().stream().filter(s -> !s.getName().equals(childNodeName)).collect(Collectors.toList());
        childElements.forEach(n -> n.detach());
        e.getChild(childNodeName, e.getNamespace()).addContent(childElements);
      }
      return e;
    };
    return f;
  }

  public static Function<Element, Element> moveAttributeToChildNode(String name, String childNodeName) {
    Function<Element, Element> f = e -> {
      if (e != null) {
        Attribute attribute = e.getAttribute(name);
        Element child = e.getChild(childNodeName, e.getNamespace());
        if (attribute != null && child != null) {
          e.removeAttribute(attribute);
          child.setAttribute(attribute);
          // TODO missing reporting MoveAttributeToChildNode.execute()
        }
      }
      return e;
    };
    return f;
  }

  public static Function<Element, Element> attributeToChildNode(String name) {
    Function<Element, Element> f = e -> {
      if (e != null) {
        Attribute attribute = e.getAttribute(name);
        if (attribute != null) {
          e.removeAttribute(attribute);

          Element child = new Element(name, e.getNamespace());
          child.setAttribute(new Attribute("value", attribute.getValue()));

          e.addContent(0, child);
          // TODO missing reporting MoveAttributeToChildNode.execute()
        }
      }
      return e;
    };
    return f;
  }

  public static Function<Element, Element> addChildNode(String nameSpace, String name) {
    Function<Element, Element> f = e -> {
      Namespace ns = e.getDocument().getRootElement().getNamespace(nameSpace);
      if (e.getChild(name, ns) == null) {
        e.addContent(new Element(name, ns));
        // TODO missing reporting CreateChildNode.execute
      } else {
        // TODO missing reporting CreateChildNode.execute
      }
      return e;
    };

    return f;

  }

  public static Function<Element, Element> updateMUnitAssertionEqualsExpression(String attributeName) {
    Function<Element, Element> f = e -> {
      Attribute attribute = e.getAttribute(attributeName);
      if (attribute != null) {
        String attributeValue = attribute.getValue().trim();
        if (attributeValue.startsWith("#[")) {
          StringBuffer sb = new StringBuffer(attributeValue);
          sb.replace(0, sb.indexOf("[") + 1, "#[MUnitTools::equalTo(");
          sb.replace(sb.lastIndexOf("]"), sb.lastIndexOf("]") + 1, ")]");
          attributeValue = sb.toString();
        } else {
          attributeValue = "#[MUnitTools::equalTo(" + attributeValue + ")]";
        }
        attribute.setValue(attributeValue);
      }
      return e;
    };
    return f;
  }

  public static Function<Element, Element> updateMUnitAssertionNotEqualsExpression(String attributeName) {
    Function<Element, Element> f = e -> {
      Attribute attribute = e.getAttribute(attributeName);
      if (attribute != null) {
        String attributeValue = attribute.getValue().trim();
        if (attributeValue.startsWith("#[")) {
          StringBuffer sb = new StringBuffer(attributeValue);
          sb.replace(0, sb.indexOf("[") + 1, "#[MunitTools::not(MUnitTools::equalTo(");
          sb.replace(sb.lastIndexOf("]"), sb.lastIndexOf("]") + 1, "))]");
          attributeValue = sb.toString();
        } else {
          attributeValue = "#[MunitTools::not(MUnitTools::equalTo(" + attributeValue + "))]";
        }
        attribute.setValue(attributeValue);
      }
      return e;
    };
    return f;
  }

  // ******************************************************************************************************************


  @Deprecated
  public static Element findChildElement(String nodeName, String referenceValue, String targetReferenceAttribute,
                                         Namespace namespace, Element element) {

    if (element.getNamespace().equals(namespace)
        && element.getName().equals(nodeName) &&
        element.getAttributeValue(targetReferenceAttribute).equals(referenceValue)) {
      return element;
    } else {
      for (Element childElement : element.getChildren()) {
        Element child = findChildElement(nodeName, referenceValue, targetReferenceAttribute, namespace, childElement);
        if (null != child) {
          return child;
        }
      }
    }
    return null;
  }

  @Deprecated
  public static Element findChildElement(String nodeName, Element element) {

    if (element.getName().equals(nodeName)) {
      return element;
    } else {
      for (Element childElement : element.getChildren()) {
        Element child = findChildElement(nodeName, childElement);
        if (null != child) {
          return child;
        }
      }
    }
    return null;
  }
}
