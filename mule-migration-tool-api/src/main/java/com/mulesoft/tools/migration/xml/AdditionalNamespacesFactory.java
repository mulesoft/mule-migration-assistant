/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.xml;

import com.mulesoft.tools.migration.task.AbstractMigrationTask;
import com.mulesoft.tools.migration.task.MigrationStepSelector;
import com.mulesoft.tools.migration.task.MigrationTask;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Namespace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generates a {@link List<Namespace>}
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class AdditionalNamespacesFactory {

  public static List<Namespace> getDocumentNamespaces(Document document, List<Namespace> tasksSupportedNamespaces) {
    Map<String, String> namespaces = new HashMap<>();
    List<Namespace> documentNamespaces = new ArrayList<>();
    document.getRootElement().getAdditionalNamespaces().forEach(n -> namespaces.computeIfAbsent(n.getURI(), k -> n.getPrefix()));

    tasksSupportedNamespaces.stream()
        .filter(n -> namespaces.get(n.getURI()) == null)
        .filter(n -> !namespaces.containsValue(n.getPrefix()))
        .forEach(n -> namespaces.put(n.getURI(), n.getPrefix()));

    documentNamespaces.addAll(namespaces.entrySet().stream()
        .map(namespace -> Namespace.getNamespace(namespace.getValue(), namespace.getKey()))
        .collect(Collectors.toList()));

    return documentNamespaces;
  }

  public static List<Namespace> getTasksDeclaredNamespaces(List<AbstractMigrationTask> migrationTasks) {
    List<Namespace> taskSupportedNamespaces = new ArrayList<>();
    for (MigrationTask task : migrationTasks) {
      MigrationStepSelector stepSelector = new MigrationStepSelector(task.getSteps());
      stepSelector.getApplicationModelContributionSteps()
          .forEach(s -> taskSupportedNamespaces.addAll(s.getNamespacesContributions()));
    }
    return taskSupportedNamespaces;
  }

  public static boolean containsNamespace(Namespace ns, List<Namespace> tasksSupportedNamespaces) {
    return tasksSupportedNamespaces.stream().anyMatch(n -> StringUtils.equalsIgnoreCase(n.getURI(), ns.getURI()));
  }
}
