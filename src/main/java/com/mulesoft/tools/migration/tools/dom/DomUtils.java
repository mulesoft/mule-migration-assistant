/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.tools.dom;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 *
 * Utilities to work with DOM
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class DomUtils {

  // private DomUtils() {}

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
