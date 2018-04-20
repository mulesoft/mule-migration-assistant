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

import static com.mulesoft.tools.migration.project.model.ApplicationModel.getElementsWithNamespace;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static com.mulesoft.tools.migration.xml.AdditionalNamespacesFactory.getAdditionalNamespaces;

/**
 * Check for component with no defined migration task yet.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class PreprocessNamespaces implements NamespaceContribution {

  @Override
  public String getDescription() {
    return "Check components with no migration defined.";
  }

  @Override
  public void execute(ApplicationModel applicationModel, MigrationReport report) throws RuntimeException {
    List<Document> documents = applicationModel.getApplicationDocuments().values().stream().collect(Collectors.toList());
    documents.forEach(d -> addReportEntries(d, report));
  }

  public void addReportEntries(Document document, MigrationReport report) {
    List<Namespace> unsupportedNamespaces =
        document.getRootElement().getAdditionalNamespaces().stream().filter(n -> getElementsWithNamespace(document, n).size() > 0
            && !getAdditionalNamespaces().contains(n)).collect(Collectors.toList());

    unsupportedNamespaces.forEach(n -> report.report(WARN, document.getRootElement(), document.getRootElement(),
                                                     "Didn't find migration rules for the following component: " + n.getPrefix(),
                                                     "https://docs.mulesoft.com/mule-user-guide/"));
  }
}
