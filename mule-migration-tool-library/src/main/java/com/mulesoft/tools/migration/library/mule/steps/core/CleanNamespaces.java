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

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.project.model.ApplicationModel.getElementsWithNamespace;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.category.NamespaceContribution;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Namespace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
