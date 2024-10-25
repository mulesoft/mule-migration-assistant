/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.json;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.CORE_EE_NAMESPACE;
import static com.mulesoft.tools.migration.step.util.XmlDslUtils.EE_NAMESPACE_SCHEMA;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate XML to JSON transformer
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class XmlToJsonTransformer extends AbstractApplicationModelMigrationStep implements JsonMigrationStep {

  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + JSON_NAMESPACE_URI + "'"
      + " and local-name()='xml-to-json-transformer']";

  @Override
  public String getDescription() {
    return "Migrate XML to JSON transformer";
  }

  public XmlToJsonTransformer() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(JSON_NAMESPACE));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    getApplicationModel().addNameSpace(CORE_EE_NAMESPACE, EE_NAMESPACE_SCHEMA, element.getDocument());
    element.setName("transform");
    element.setNamespace(CORE_EE_NAMESPACE);
    element.removeContent();
    element.addContent(new Element("message", CORE_EE_NAMESPACE)
        .addContent(new Element("set-payload", CORE_EE_NAMESPACE)
            .setText("%dw 2.0 output application/json --- payload")));

    element.removeAttribute("name");
  }

}
