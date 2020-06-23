/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.xml;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_NAMESPACE;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.jdom2.Namespace.getNamespace;

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
    document.getRootElement().getAdditionalNamespaces().forEach(n -> {
      if (!StringUtils.isEmpty(n.getPrefix())) {
        namespaces.computeIfAbsent(n.getURI(), k -> n.getPrefix());
      }
    });

    if (tasksSupportedNamespaces != null) {
      tasksSupportedNamespaces.stream()
          .filter(n -> namespaces.get(n.getURI()) == null)
          .filter(n -> !namespaces.containsValue(n.getPrefix()))
          .forEach(n -> namespaces.put(n.getURI(), n.getPrefix()));
    }

    documentNamespaces.addAll(namespaces.entrySet().stream()
        .map(namespace -> getNamespace(namespace.getValue(), namespace.getKey()))
        .collect(toList()));

    // Add the prefix commonly used in the XPath expressions
    documentNamespaces.add(getNamespace("mule", CORE_NAMESPACE.getURI()));

    return documentNamespaces;
  }

  public static List<Namespace> getTasksDeclaredNamespaces(List<AbstractMigrationTask> migrationTasks) {
    List<Namespace> taskSupportedNamespaces = new ArrayList<>();
    for (MigrationTask task : ofNullable(migrationTasks).orElse(emptyList())) {
      MigrationStepSelector stepSelector = new MigrationStepSelector(task.getSteps());
      stepSelector.getApplicationModelContributionSteps()
          .forEach(s -> taskSupportedNamespaces.addAll(s.getNamespacesContributions()));
    }
    taskSupportedNamespaces.add(getNamespace("mule", CORE_NAMESPACE.getURI()));
    return taskSupportedNamespaces;
  }

  public static boolean containsNamespace(Namespace ns, List<Namespace> tasksSupportedNamespaces) {
    if (tasksSupportedNamespaces != null) {
      return tasksSupportedNamespaces.stream().anyMatch(n -> StringUtils.equalsIgnoreCase(n.getURI(), ns.getURI()));
    } else {
      return false;
    }
  }
}
