/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.xml;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Namespace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Generates a {@link List<Namespace>}
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class AdditionalNamespacesFactory {

  public static List<Namespace> getAdditionalNamespaces() {
    List<Namespace> namespaces = new ArrayList<>();
    Arrays.stream(AdditionalNamespaces.values()).forEach(n -> namespaces.add(Namespace.getNamespace(n.prefix(), n.uri())));
    return namespaces;
  }

  public static List<Namespace> getDocumentNamespaces(Document document) {
    Map<String, String> namespaces = new HashMap<>();
    List<Namespace> documentNamespaces = new ArrayList<>();
    document.getRootElement().getAdditionalNamespaces().forEach(n -> namespaces.computeIfAbsent(n.getURI(), k -> n.getPrefix()));
    getAdditionalNamespaces().forEach(n -> namespaces.computeIfAbsent(n.getURI(), k -> n.getPrefix()));

    documentNamespaces.addAll(namespaces.entrySet().stream()
        .map(namespace -> Namespace.getNamespace(namespace.getValue(), namespace.getKey())).collect(Collectors.toList()));

    return documentNamespaces;
  }

  public static boolean containsNamespace(Namespace ns) {
    List<Namespace> validNamespaces = getAdditionalNamespaces();
    return validNamespaces.stream().anyMatch(n -> StringUtils.equalsIgnoreCase(n.getURI(), ns.getURI()));
  }
}
