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
package com.mulesoft.tools.migration.library.mule.steps.spring;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.apache.commons.lang3.RandomUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.ArrayList;
import java.util.List;

/**
 * Migrates the rest of spring elements.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SpringContributions extends AbstractSpringMigratorStep {

  public static final String ADDITIONAL_SPRING_NAMESPACES_PROP = "mule.migration.additionalSpringNamespaces";

  private static final String ADDITIONAL_SPRING_NAMESPACES = System.getProperty(ADDITIONAL_SPRING_NAMESPACES_PROP);

  public static final String XPATH_SELECTOR =
      "/*[starts-with(namespace-uri(), 'http://www.mulesoft.org/schema/mule/')]/*[starts-with(namespace-uri(), 'http://www.springframework.org/schema') %s]";

  @Override
  public String getDescription() {
    return "Migrates the rest of spring elements.";
  }

  public SpringContributions() {
    if (ADDITIONAL_SPRING_NAMESPACES == null) {
      this.setAppliedTo(format(XPATH_SELECTOR, ""));
      this.setNamespacesContributions(singletonList(getNamespace("ss", "http://www.springframework.org/schema/security")));
    } else {
      // The app may be using a lib that declares a Spring namespace handler and elements of that namespace may be present in the
      // app.
      // We move those elements to the spring file so that spring handles them.
      List<Namespace> additionalSpringNamespaces = new ArrayList<>();
      additionalSpringNamespaces.add(getNamespace("ss", "http://www.springframework.org/schema/security"));
      additionalSpringNamespaces.addAll(stream(ADDITIONAL_SPRING_NAMESPACES.split(","))
          .map(nsUri -> {
            String[] uriParts = nsUri.split("\\/");
            return getNamespace(uriParts[uriParts.length - 1] + "_" + RandomUtils.nextInt(), nsUri);
          })
          .collect(toList()));

      this.setAppliedTo(format(XPATH_SELECTOR, additionalSpringNamespaces.stream()
          .map(ns -> "namespace-uri() = '" + ns.getURI() + "'").collect(joining(" or ", "or ", ""))));
      this.setNamespacesContributions(additionalSpringNamespaces);
    }
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Document muleDocument = object.getDocument();
    Document springDocument = resolveSpringDocument(muleDocument);

    object.detach();
    springDocument.getRootElement().addContent(object);
    moveNamespacesDeclarations(muleDocument, object, springDocument);
  }
}
