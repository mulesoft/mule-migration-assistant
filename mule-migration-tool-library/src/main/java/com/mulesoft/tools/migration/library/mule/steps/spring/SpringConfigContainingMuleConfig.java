/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.spring;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.library.mule.steps.spring.SpringBeans.SPRING_BEANS_NS_URI;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Migrates the spring configuration from the mule config to its own file.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class SpringConfigContainingMuleConfig extends AbstractSpringMigratorStep {

  public static final String XPATH_SELECTOR =
      "/*[namespace-uri()='" + SPRING_BEANS_NS_URI + "' and local-name()='beans']/mule:mule";

  @Override
  public String getDescription() {
    return "Migrates the outer spring config to its own file.";
  }

  public SpringConfigContainingMuleConfig() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(SPRING_NAMESPACE));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    Document muleDocuemnt = object.getDocument();
    Document springDocument = resolveSpringDocument(muleDocuemnt);

    Document muleDoc = object.getDocument();
    Element springRoot = object.getDocument().detachRootElement();
    object.detach();

    springDocument.setRootElement(springRoot);
    muleDoc.setRootElement(object);
    object.addContent(1, springDocument.getRootElement().getChild("config", SPRING_NAMESPACE).detach());

    moveNamespacesDeclarations(muleDocuemnt, springDocument.getRootElement(), springDocument);
  }

}
