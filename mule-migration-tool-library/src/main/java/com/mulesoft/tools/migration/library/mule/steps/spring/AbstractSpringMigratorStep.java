/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.spring;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Arrays.stream;
import static org.jdom2.Namespace.getNamespace;

/**
 * Common stuff for migrators of Spring elements
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
abstract class AbstractSpringMigratorStep extends AbstractApplicationModelMigrationStep {

  private static final String SPRING_FOLDER = "src/main/resources/spring/";

  protected static final String SPRING_NAMESPACE_PREFIX = "spring-module";
  protected static final String SPRING_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/spring";
  protected static final Namespace SPRING_NAMESPACE = getNamespace(SPRING_NAMESPACE_PREFIX, SPRING_NAMESPACE_URI);
  protected static final Namespace SPRING_SECURITY_NAMESPACE =
      Namespace.getNamespace("http://www.mulesoft.org/schema/mule/spring-security");

  protected Document resolveSpringDocument(Document currentDoc) {
    Path beansPath = null;
    Document springDocument = null;

    // Check if a spring file already exists for this mule config
    for (Entry<Path, Document> entry : getApplicationModel().getApplicationDocuments().entrySet()) {
      if (currentDoc.equals(entry.getValue())) {
        beansPath = resolveSpringBeansPath(entry);

        if (getApplicationModel().getApplicationDocuments()
            .containsKey(Paths.get(SPRING_FOLDER + beansPath.getFileName().toString()))) {
          return getApplicationModel().getApplicationDocuments()
              .get(Paths.get(SPRING_FOLDER + beansPath.getFileName().toString()));
        }
      }
    }

    // If not, create it and link it
    for (Entry<Path, Document> entry : getApplicationModel().getApplicationDocuments().entrySet()) {
      if (currentDoc.equals(entry.getValue())) {
        beansPath = resolveSpringBeansPath(entry);

        try {
          SAXBuilder saxBuilder = new SAXBuilder();
          springDocument =
              saxBuilder.build(AbstractSpringMigratorStep.class.getClassLoader().getResourceAsStream("spring/empty-beans.xml"));
        } catch (JDOMException | IOException e) {
          throw new MigrationStepException(e.getMessage(), e);
        }

        addSpringModuleConfig(currentDoc.getRootElement(), "spring/" + beansPath.getFileName().toString());
        break;
      }
    }

    if (beansPath == null) {
      throw new MigrationStepException("The document of the passed element was not present in the application model");
    }

    getApplicationModel().getApplicationDocuments()
        .put(Paths.get(SPRING_FOLDER + beansPath.getFileName().toString()), springDocument);

    return springDocument;
  }

  protected void moveNamespacesDeclarations(Document muleDocument, Element movedElement, Document springDocument) {
    Set<Namespace> declaredNamespaces = doGetNamespacesDeclarationsRecursively(movedElement);

    Attribute schemaLocationAttribute =
        muleDocument.getRootElement().getAttribute("schemaLocation", muleDocument.getRootElement().getNamespace("xsi"));

    if (schemaLocationAttribute != null) {
      Map<String, String> locations = new HashMap<>();
      String[] splitLocations = stream(schemaLocationAttribute.getValue().split("\\s")).filter(s -> !StringUtils.isEmpty(s))
          .toArray(String[]::new);

      for (int i = 0; i < splitLocations.length; i += 2) {
        locations.put(splitLocations[i], splitLocations[i + 1]);
      }

      for (Namespace namespace : declaredNamespaces) {
        if (!StringUtils.isEmpty(namespace.getURI())) {
          getApplicationModel().addNameSpace(namespace, locations.get(namespace.getURI()), springDocument.getDocument());
        }
      }
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

  protected void addSpringModuleConfig(Element rootElement, String beansPath) {
    Element config = new Element("config", SPRING_NAMESPACE);
    config.setAttribute("name", "springConfig");
    config.setAttribute("files", beansPath);
    rootElement.addContent(0, config);

    getApplicationModel().addNameSpace(SPRING_NAMESPACE, "http://www.mulesoft.org/schema/mule/spring/current/mule-spring.xsd",
                                       rootElement.getDocument());
  }

  private Path resolveSpringBeansPath(Entry<Path, Document> entry) {
    if (entry.getKey().getParent() != null) {
      return entry.getKey().getParent().resolve(entry.getKey().getFileName().toString().replace(".xml", "-beans.xml"));
    } else {
      return Paths.get(entry.getKey().getFileName().toString().replace(".xml", "-beans.xml"));
    }
  }
}
