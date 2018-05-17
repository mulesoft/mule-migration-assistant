/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.springsecurity;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;

import org.jdom2.Namespace;

/**
 * Common stuff for migrators of Spring elements
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
abstract class AbstractSpringSecurityMigratorStep extends AbstractApplicationModelMigrationStep {

  protected static final Namespace SPRING_SECURITY_NAMESPACE =
      Namespace.getNamespace("http://www.mulesoft.org/schema/mule/spring-security");
  protected static final Namespace SPRING_MODULE_NAMESPACE =
      Namespace.getNamespace("spring-module", "http://www.mulesoft.org/schema/mule/spring");

  // protected Document resolveSpringDocument(Document currentDoc) {
  // Path beansPath = null;
  // Document springDocument = null;
  //
  // // Check if a spring file already exists for this mule config
  // for (Entry<Path, Document> entry : getApplicationModel().getApplicationDocuments().entrySet()) {
  // if (currentDoc.equals(entry.getValue())) {
  // beansPath = resolveSpringBeansPath(entry);
  //
  // if (getApplicationModel().getApplicationDocuments()
  // .containsKey(Paths.get("src/main/resources/spring/" + beansPath.getFileName().toString()))) {
  // return getApplicationModel().getApplicationDocuments()
  // .get(Paths.get("src/main/resources/spring/" + beansPath.getFileName().toString()));
  // }
  // }
  // }
  //
  // // If not, create it and link it
  // for (Entry<Path, Document> entry : getApplicationModel().getApplicationDocuments().entrySet()) {
  // if (currentDoc.equals(entry.getValue())) {
  // beansPath = resolveSpringBeansPath(entry);
  //
  // try {
  // SAXBuilder saxBuilder = new SAXBuilder();
  // springDocument =
  // saxBuilder.build(AbstractSpringSecurityMigratorStep.class.getClassLoader().getResourceAsStream("spring/empty-beans.xml"));
  // } catch (JDOMException | IOException e) {
  // throw new MigrationStepException(e.getMessage(), e);
  // }
  //
  // addSpringModuleConfig(currentDoc.getRootElement(), "spring/" + beansPath.getFileName().toString());
  // break;
  // }
  // }
  //
  // if (beansPath == null) {
  // throw new MigrationStepException("The document of the passed element was not present in the application model");
  // }
  //
  // getApplicationModel().getApplicationDocuments()
  // .put(Paths.get("src/main/resources/spring/" + beansPath.getFileName().toString()), springDocument);
  // return springDocument;
  // }
  //
  // protected void addSpringModuleConfig(Element rootElement, String beansPath) {
  // Element config = new Element("config", SPRING_NAMESPACE);
  // config.setAttribute("name", "springConfig");
  // config.setAttribute("files", beansPath);
  // rootElement.addContent(0, config);
  //
  // getApplicationModel().addNameSpace(SPRING_NAMESPACE, "http://www.mulesoft.org/schema/mule/spring/current/mule-spring.xsd",
  // rootElement.getDocument());
  // }
  //
  // private Path resolveSpringBeansPath(Entry<Path, Document> entry) {
  // if (entry.getKey().getParent() != null) {
  // return entry.getKey().getParent().resolve(entry.getKey().getFileName().toString().replace(".xml", "-beans.xml"));
  // } else {
  // return Paths.get(entry.getKey().getFileName().toString().replace(".xml", "-beans.xml"));
  // }
  // }
}
