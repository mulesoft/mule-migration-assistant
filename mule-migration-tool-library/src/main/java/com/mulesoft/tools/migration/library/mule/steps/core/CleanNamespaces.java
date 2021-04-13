/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.project.model.ApplicationModel.getElementsWithNamespace;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.NamespaceContribution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Namespace;

/**
 * Remove unusued namespaces
 *
 * @author Mulesoft Inc.
 * @since 2.0.0
 */
public class CleanNamespaces implements NamespaceContribution {

  private final List<String> nonRemovableNamespaces = newArrayList("doc", "xsi");

  @Override
  public String getDescription() {
    return "Remove unused namespaces declarations.";
  }

  @Override
  public void execute(ApplicationModel applicationModel, MigrationReport report) throws RuntimeException {
    List<Document> documents = applicationModel.getApplicationDocuments().values().stream().collect(Collectors.toList());
    documents.forEach(d -> removeUnusedNamespacesAndSchemas(d, applicationModel));
  }

  public void removeUnusedNamespacesAndSchemas(Document document, ApplicationModel applicationModel) {
    List<Namespace> unusedNamespaces = document.getRootElement().getAdditionalNamespaces().stream()
        .filter(n -> getElementsWithNamespace(document, n, applicationModel).size() <= 0
            && !nonRemovableNamespaces.contains(n.getPrefix()))
        .collect(Collectors.toList());
    unusedNamespaces.forEach(n -> document.getRootElement().removeNamespaceDeclaration(n));

    Attribute schemaLocationAttribute = document.getRootElement()
        .getAttribute("schemaLocation", document.getRootElement().getNamespace("xsi"));

    Map<String, String> schemas = new HashMap<>();

    String[] schemaValue = schemaLocationAttribute.getValue().split("\\s+");
    for (int i = 0; i < schemaValue.length; i++) {
      if (!schemaValue[i].equals("")) {
        schemas.put(schemaValue[i], schemaValue[i + 1]);
        i++;
      }
    }
    unusedNamespaces.forEach(n -> schemas.remove(n.getURI()));

    StringBuilder usedSchemas = new StringBuilder();
    schemas.forEach((url, schema) -> {
      usedSchemas.append(url);
      usedSchemas.append(" " + schema + " ");
    });

    schemaLocationAttribute.setValue(usedSchemas.toString().trim());
  }
}
