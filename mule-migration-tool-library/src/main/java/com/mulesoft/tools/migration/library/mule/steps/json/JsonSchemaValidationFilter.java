/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.mule.steps.json;

import static com.google.common.collect.Lists.newArrayList;

import com.mulesoft.tools.migration.library.mule.steps.core.filter.AbstractFilterMigrator;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Migrate deprecated JSON Schema validation filter
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class JsonSchemaValidationFilter extends AbstractFilterMigrator implements JsonMigrationStep {

  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + JSON_NAMESPACE_URI + "'"
      + " and local-name()='json-schema-validation-filter']";

  @Override
  public String getDescription() {
    return "Migrate deprecated JSON Schema validation filter";
  }

  public JsonSchemaValidationFilter() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(JSON_NAMESPACE));
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
