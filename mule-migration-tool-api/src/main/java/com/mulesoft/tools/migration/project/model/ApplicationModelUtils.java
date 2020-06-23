/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
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

  /**
   * Changes node name and namespace.
   *
   * @param nameSpace the new element namespace
   * @param name      the new element name
   * @return a function that accepts an element and returns it with updated namespace and name
   */
  public static Function<Element, Element> changeNodeName(String nameSpace, String name) {
    return e -> {
      if (e != null) {
        e.setNamespace(e.getDocument().getRootElement().getNamespace(nameSpace));
        e.setName(name);
      }
      return e;
    };
  }

  /**
   * Adds an attribute to the element accepted by the function.
   *
   * @param name  the attribute name
   * @param value the attribute value
   * @return a function that accepts an element and returns it with the attribute
   */
  public static Function<Element, Element> addAttribute(String name, String value) {
    return e -> {
      if (e != null) {
        Attribute att = new Attribute(name, value);
        e.setAttribute(att);
      }
      return e;
    };
  }

  /**
   * Updates an element attribute on the element accepted by the function. Either name or value can be updated.
   *
   * @param name    the name of the attribute to be updated
   * @param newName the new attribute name (optional)
   * @param value   the new attribute value (optional)
   * @return a function that accepts an element and returns it updated accordingly
   */
  public static Function<Element, Element> changeAttribute(String name, Optional<String> newName, Optional<String> value) {
    return e -> {
      if (e != null) {
        Attribute attribute = e.getAttribute(name);
        if (attribute != null) {
          newName.ifPresent(nn -> attribute.setName(nn));
          value.ifPresent(v -> attribute.setValue(v));
        }
      }
      return e;
    };
  }

  /**
   * Removes an attribute from the element accepted by the function.
   *
   * @param name the name of the attribute to be removed
   * @return a function that accepts an element and returns it without the specified attribute, or null if the attribute is not present in the element
   */
  public static Function<Element, Element> removeAttribute(String name) {
    return e -> {
      if (e != null) {
        Attribute attribute = e.getAttribute(name);
        if (attribute != null) {
          e.removeAttribute(name);
        }
      }
      return e;
    };
  }

  /**
   * Moves the content that does not have the {@param childNodeName} as contents of the first children of the element accepted by the function.
   *
   * @param childNodeName the name of the first children that is going to receive the content
   * @return a function that accepts an element and returns it with the new structure
   */
  public static Function<Element, Element> moveContentToChild(String childNodeName) {
    return e -> {
      if (e != null) {
        List<Element> childElements =
            e.getChildren().stream().filter(s -> !s.getName().equals(childNodeName)).collect(Collectors.toList());
        childElements.forEach(Element::detach);
        e.getChild(childNodeName, e.getNamespace()).addContent(childElements);
      }
      return e;
    };
  }

  /**
   * Moves to the first child with the specified name an attribute with {@param name} of the element accepted by the function.
   *
   * @param name          the attribute name
   * @param childNodeName the child name
   * @return a function that accepts an element and returns it with the attribute moved to the specified child
   */
  public static Function<Element, Element> moveAttributeToChildNode(String name, String childNodeName) {
    return e -> {
      if (e != null) {
        Attribute attribute = e.getAttribute(name);
        Element child = e.getChild(childNodeName, e.getNamespace());
        if (attribute != null && child != null) {
          e.removeAttribute(attribute);
          child.setAttribute(attribute);
        }
      }
      return e;
    };
  }

  /**
   * Transforms an attribute into a child.
   *
   * @param name the name of the attribute to be turned into a child
   * @return a function that accepts an element and returns it with the attribute turned into a child
   */
  public static Function<Element, Element> attributeToChildNode(String name) {
    return e -> {
      if (e != null) {
        Attribute attribute = e.getAttribute(name);
        if (attribute != null) {
          e.removeAttribute(attribute);

          Element child = new Element(name, e.getNamespace());
          child.setAttribute(new Attribute("value", attribute.getValue()));

          e.addContent(0, child);
        }
      }
      return e;
    };
  }

  /**
   * Adds a child node with the specified namespace and name.
   *
   * @param namespace the new child namespace
   * @param name      the new child name
   * @return a function that accepts an element and returns it with a new child set with {@param namespace} and {@param name}
   */
  public static Function<Element, Element> addChildNode(String namespace, String name) {
    return e -> {
      Namespace ns = e.getDocument().getRootElement().getNamespace(namespace);
      if (e.getChild(name, ns) == null) {
        e.addContent(new Element(name, ns));
      }
      return e;
    };
  }


}
