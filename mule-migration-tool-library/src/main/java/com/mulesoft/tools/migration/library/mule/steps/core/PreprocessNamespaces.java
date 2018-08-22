/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.library.mule.steps.spring.SpringContributions.ADDITIONAL_SPRING_NAMESPACES_PROP;
import static com.mulesoft.tools.migration.project.model.ApplicationModel.getElementsWithNamespace;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.xml.AdditionalNamespacesFactory.containsNamespace;
import static java.lang.System.lineSeparator;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.NamespaceContribution;

import org.jdom2.Document;
import org.jdom2.Namespace;

import java.util.List;
import java.util.stream.Collectors;

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
    documents.forEach(d -> addReportEntries(d, report, applicationModel));
  }

  public void addReportEntries(Document document, MigrationReport report, ApplicationModel applicationModel) {
    List<Namespace> unsupportedNamespaces =
        document.getRootElement().getAdditionalNamespaces().stream()
            .filter(n -> !getElementsWithNamespace(document, n, applicationModel).isEmpty()
                && !containsNamespace(n, applicationModel.getSupportedNamespaces()))
            .collect(Collectors.toList());


    unsupportedNamespaces.forEach(n -> {
      if (n.getURI().startsWith("http://www.mulesoft.org")) {
        report.report(ERROR, document.getRootElement(), document.getRootElement(),
                      "The migration of " + n.getPrefix() + " is not supported.",
                      "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors",
                      "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-tool#unsupported_connectors");
      } else {
        report.report(ERROR, document.getRootElement(), document.getRootElement(),
                      "Didn't find migration rules for the following component: " + n.getPrefix()
                          + "." + lineSeparator()
                          + "If that component is defined in a Spring namespace handler of an app dependency, include its uri ("
                          + n.getURI() + ") in the '" + ADDITIONAL_SPRING_NAMESPACES_PROP
                          + "' so it is handled by the Spring Module.",
                      "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-connectors",
                      "https://docs.mulesoft.com/connectors/spring-module");
      }
    });
  }
}
