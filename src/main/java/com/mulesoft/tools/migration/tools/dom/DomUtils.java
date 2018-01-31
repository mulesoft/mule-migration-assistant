/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.tools.dom;

import org.jdom2.Element;
import org.jdom2.Namespace;

public class DomUtils {

    private DomUtils() {
    }

    public static Element findChildElement(String nodeName, String referenceValue, String targetReferenceAttribute, Namespace namespace, Element element) {

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
