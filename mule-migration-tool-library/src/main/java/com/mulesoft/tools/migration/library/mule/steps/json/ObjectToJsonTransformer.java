/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.json;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_EE_NAMESPACE;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrate Object to JSON transformer
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ObjectToJsonTransformer extends AbstractApplicationModelMigrationStep {

  private static final String JSON_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/json";
  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + JSON_NAMESPACE_URI + "'"
      + " and local-name()='object-to-json-transformer']";

  @Override
  public String getDescription() {
    return "Migrate Object to JSON transformer";
  }

  public ObjectToJsonTransformer() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(Namespace.getNamespace("json", JSON_NAMESPACE_URI)));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    element.setName("transform");
    element.setNamespace(CORE_EE_NAMESPACE);
    element.removeContent();
    element.addContent(new Element("message", CORE_EE_NAMESPACE)
        .addContent(new Element("set-payload", CORE_EE_NAMESPACE)
            .setText("%dw 2.0 output application/json --- payload")));

    element.removeAttribute("mapper-ref");
    element.removeAttribute("sourceClass");
    element.removeAttribute("name");
  }

}
