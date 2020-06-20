/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
