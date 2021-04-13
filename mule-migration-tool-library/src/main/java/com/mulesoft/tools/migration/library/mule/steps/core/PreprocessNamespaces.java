/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.library.mule.steps.spring.SpringContributions.ADDITIONAL_SPRING_NAMESPACES_PROP;
import static com.mulesoft.tools.migration.project.model.ApplicationModel.getElementsWithNamespace;
import static com.mulesoft.tools.migration.xml.AdditionalNamespacesFactory.containsNamespace;
import static java.util.stream.Collectors.toList;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.NamespaceContribution;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.jdom2.Document;
import org.jdom2.Namespace;

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
    List<Document> documents = applicationModel.getApplicationDocuments().values().stream().collect(toList());
    documents.forEach(d -> addReportEntries(d, report, applicationModel));
  }

  public void addReportEntries(Document document, MigrationReport report, ApplicationModel applicationModel) {
    List<Namespace> unsupportedNamespaces =
        document.getRootElement().getAdditionalNamespaces().stream()
            .filter(n -> !getElementsWithNamespace(document, n, applicationModel).isEmpty()
                && !containsNamespace(n, applicationModel.getSupportedNamespaces()))
            .collect(toList());

    AtomicInteger processedElements = new AtomicInteger(0);

    unsupportedNamespaces.forEach(ns -> {
      // Ignore nested elements of the same pass to not distort statistics or clutter the report
      applicationModel.getNodes("//*[namespace-uri() = '" + ns.getURI() + "' and namespace-uri(..) != '" + ns.getURI() + "']")
          .forEach(node -> {
            processedElements.incrementAndGet();

            if (ns.getURI().startsWith("http://www.mulesoft.org")) {
              report.report("components.unsupported", node, node, ns.getPrefix());
            } else {
              report.report("components.unknown", node, node, ns.getPrefix(), ns.getURI(), ADDITIONAL_SPRING_NAMESPACES_PROP);
            }
          });
    });

    report.addProcessedElements(processedElements.get());
  }
}
