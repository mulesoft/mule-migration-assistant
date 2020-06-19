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

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.addTopLevelElement;
import static java.util.Arrays.stream;
import static org.apache.commons.lang3.StringUtils.substring;
import static org.jdom2.Namespace.getNamespace;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

/**
 * Common stuff for migrators of Spring elements
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
abstract class AbstractSpringMigratorStep extends AbstractApplicationModelMigrationStep {

  private static final String SPRING_FOLDER =
      "src" + File.separator + "main" + File.separator + "resources" + File.separator + "spring" + File.separator;

  protected static final String SPRING_NAMESPACE_PREFIX = "spring-module";
  protected static final String SPRING_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/spring";
  protected static final Namespace SPRING_NAMESPACE = getNamespace(SPRING_NAMESPACE_PREFIX, SPRING_NAMESPACE_URI);
  protected static final Namespace SPRING_SECURITY_NAMESPACE =
      getNamespace("mule-ss", "http://www.mulesoft.org/schema/mule/spring-security");

  protected Document resolveSpringDocument(Document currentDoc) {
    Document springDocument = null;

    springDocument = resolveSpringFile(currentDoc, springDocument, getApplicationModel().getApplicationDocuments());
    if (springDocument == null) {
      springDocument = resolveSpringFile(currentDoc, springDocument, getApplicationModel().getDomainDocuments());
      if (springDocument == null) {
        throw new MigrationStepException("The document of the passed element was not present in the application model");
      }
    }

    return springDocument;
  }

  protected Document resolveSpringFile(Document currentDoc, Document springDocument, final Map<Path, Document> artifactDocs) {
    Path beansPath = null;

    // Check if a spring file already exists for this mule config
    for (Entry<Path, Document> entry : artifactDocs.entrySet()) {
      if (currentDoc.equals(entry.getValue())) {
        beansPath = resolveSpringBeansPath(entry);

        if (artifactDocs.containsKey(Paths.get(SPRING_FOLDER + beansPath.getFileName().toString()))) {
          return artifactDocs.get(Paths.get(SPRING_FOLDER + beansPath.getFileName().toString()));
        }
      }
    }

    // If not, create it and link it
    for (Entry<Path, Document> entry : artifactDocs.entrySet()) {
      if (currentDoc.equals(entry.getValue())) {
        beansPath = resolveSpringBeansPath(entry);

        try {
          SAXBuilder saxBuilder = new SAXBuilder();
          springDocument =
              saxBuilder.build(AbstractSpringMigratorStep.class.getClassLoader().getResourceAsStream("spring/empty-beans.xml"));
        } catch (JDOMException | IOException e) {
          throw new MigrationStepException(e.getMessage(), e);
        }

        addSpringModuleConfig(currentDoc, "spring/" + beansPath.getFileName().toString());
        break;
      }
    }

    if (beansPath == null) {
      return null;
    }

    artifactDocs.put(Paths.get(SPRING_FOLDER + beansPath.getFileName().toString()), springDocument);

    return springDocument;
  }

  protected void moveNamespacesDeclarations(Document muleDocument, Element movedElement, Document springDocument) {
    Set<Namespace> declaredNamespaces = doGetNamespacesDeclarationsRecursively(movedElement);

    Attribute schemaLocationAttribute =
        muleDocument.getRootElement().getAttribute("schemaLocation", muleDocument.getRootElement().getNamespace("xsi"));

    if (schemaLocationAttribute != null) {
      Map<String, String> locations = new HashMap<>();
      String[] splitLocations =
          stream(schemaLocationAttribute.getValue().split("\\s")).filter(s -> !StringUtils.isEmpty(s)).toArray(String[]::new);

      for (int i = 0; i < splitLocations.length; i += 2) {
        locations.put(splitLocations[i], splitLocations[i + 1]);
      }

      for (Namespace namespace : declaredNamespaces) {
        if (!StringUtils.isEmpty(namespace.getURI()) && locations.containsKey(namespace.getURI())) {
          getApplicationModel().addNameSpace(namespace, fixSpringSchemaLocationVersion(locations.get(namespace.getURI())),
                                             springDocument.getDocument());
        }
      }
    }
  }

  private String fixSpringSchemaLocationVersion(String schemaLocation) {
    if (schemaLocation.startsWith("http://www.springframework.org/schema/") && schemaLocation.endsWith("-current.xsd")) {
      return schemaLocation.replace("-current.xsd", ".xsd");
    } else {
      return schemaLocation;
    }
  }

  private Set<Namespace> doGetNamespacesDeclarationsRecursively(Element movedElement) {
    Set<Namespace> declaredNamespaces = new TreeSet<>((o1, o2) -> o1.getURI().compareTo(o2.getURI()));

    declaredNamespaces.addAll(movedElement.getNamespacesIntroduced());

    for (Element child : movedElement.getChildren()) {
      declaredNamespaces.addAll(doGetNamespacesDeclarationsRecursively(child));
    }
    return declaredNamespaces;
  }

  protected void addSpringModuleConfig(Document document, String beansPath) {
    Element config = new Element("config", SPRING_NAMESPACE);
    String[] splitBeansPath = beansPath.split("\\/");
    config.setAttribute("name",
                        substring("springConfig" + "_" + splitBeansPath[splitBeansPath.length - 1], 0, -"-beans.xml".length()));
    config.setAttribute("files", beansPath);
    addTopLevelElement(config, document);

    getApplicationModel().addNameSpace(SPRING_NAMESPACE, "http://www.mulesoft.org/schema/mule/spring/current/mule-spring.xsd",
                                       document);
  }

  private Path resolveSpringBeansPath(Entry<Path, Document> entry) {
    if (entry.getKey().getParent() != null) {
      return entry.getKey().getParent().resolve(entry.getKey().getFileName().toString().replace(".xml", "-beans.xml"));
    } else {
      return Paths.get(entry.getKey().getFileName().toString().replace(".xml", "-beans.xml"));
    }
  }
}
