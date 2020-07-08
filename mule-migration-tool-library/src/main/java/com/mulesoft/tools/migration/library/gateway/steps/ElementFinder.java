/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Utility class to obtain an element to migrate
 *
 * @author Mulesoft Inc.
 */
public class ElementFinder {

  public static Optional<Element> findChildElement(Element root, String childName, Namespace childNamespace,
                                                   Predicate<? super Element> predicate) {
    return root.getChildren(childName, childNamespace).stream()
        .filter(predicate).findFirst();
  }

  public static Optional<Element> findChildElementWithMatchingAttributeValue(Element root, String childName,
                                                                             Namespace childNamespace, String attributeName,
                                                                             String expectedAttributeValue) {
    return findChildElement(root, childName, childNamespace, element -> element.getAttributeValue(attributeName) != null
        && element.getAttributeValue(attributeName).equals(expectedAttributeValue));
  }

  public static boolean containsElementWithMatchingAttributeValue(List<Element> elements, String attributeName,
                                                                  String expectedAttributeValue) {
    return elements.stream().anyMatch(mappingE -> mappingE.getAttributeValue(attributeName).equals(expectedAttributeValue));
  }

}
