/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.NamespaceContribution;
import org.jdom2.Document;
import org.jdom2.Namespace;

import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.project.model.ApplicationModel.getElementsWithNamespace;

/**
 * Remove unusued namespaces
 *
 * @author Mulesoft Inc.
 * @since 2.0.0
 */
public class CleanNamespaces implements NamespaceContribution {

  private List<String> nonRemovableNamespaces = newArrayList("doc", "xsi");

  @Override
  public String getDescription() {
    return "Remove unused namespaces declarations.";
  }

  @Override
  public void execute(ApplicationModel applicationModel, MigrationReport report) throws RuntimeException {
    List<Document> documents = applicationModel.getApplicationDocuments().values().stream().collect(Collectors.toList());
    documents.forEach(d -> removeUnusedNamespaces(d, applicationModel));
  }


  public void removeUnusedNamespaces(Document document, ApplicationModel applicationModel) {
    List<Namespace> unusedNamespaces = document.getRootElement().getAdditionalNamespaces().stream()
        .filter(n -> getElementsWithNamespace(document, n, applicationModel).size() <= 0
            && !nonRemovableNamespaces.contains(n.getPrefix()))
        .collect(Collectors.toList());
    unusedNamespaces.forEach(n -> document.getRootElement().removeNamespaceDeclaration(n));
  }
}
