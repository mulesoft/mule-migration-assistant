/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.json;

import static com.google.common.collect.Lists.newArrayList;

import com.mulesoft.tools.migration.library.mule.steps.core.filter.AbstractFilterMigrator;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Migrate deprecated JSON Schema validation filter
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class JsonSchemaValidationFilter extends AbstractFilterMigrator {

  private static final String JSON_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/json";
  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + JSON_NAMESPACE_URI + "'"
      + " and local-name()='json-schema-validation-filter']";

  @Override
  public String getDescription() {
    return "Migrate deprecated JSON Schema validation filter";
  }

  public JsonSchemaValidationFilter() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(Namespace.getNamespace("json", JSON_NAMESPACE_URI)));
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    element.setName("validate-schema");
    Attribute schemaLocations = element.getAttribute("schemaLocations");
    schemaLocations.setName("schema");

    String[] locations = schemaLocations.getValue().split(",");
    if (locations.length > 1) {
      for (String location : locations) {
        element.getParentElement().addContent(element.getParentElement().indexOf(element),
                                              element.clone().setAttribute("schema", location));
      }
      handleFilter(element);
      element.detach();
    } else {
      handleFilter(element);
    }

  }

}
