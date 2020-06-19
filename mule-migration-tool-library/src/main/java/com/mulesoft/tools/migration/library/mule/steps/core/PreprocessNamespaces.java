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
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.mulesoft.tools.migration.library.mule.steps.spring.SpringContributions.ADDITIONAL_SPRING_NAMESPACES_PROP;
import static com.mulesoft.tools.migration.project.model.ApplicationModel.getElementsWithNamespace;
import static com.mulesoft.tools.migration.xml.AdditionalNamespacesFactory.containsNamespace;
import static java.util.stream.Collectors.toList;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.NamespaceContribution;

import org.jdom2.Document;
import org.jdom2.Namespace;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
