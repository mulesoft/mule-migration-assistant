/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.xml;

import org.jdom2.Namespace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
}
