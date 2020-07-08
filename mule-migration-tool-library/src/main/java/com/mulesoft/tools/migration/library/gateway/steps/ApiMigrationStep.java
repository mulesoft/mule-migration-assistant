/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_3_GATEWAY_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_GATEWAY_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_DOC_NAMESPACE;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.stream.Collectors;

import org.jdom2.DataConversionException;
import org.jdom2.Element;

/**
 * Migrate api element
 *
 * @author Mulesoft Inc.
 */
public class ApiMigrationStep extends GatewayMigrationStep {

  private static final String MULE_3_TAG_NAME = "api";
  private static final String MULE_4_TAG_NAME = "autodiscovery";
  private static final String FLOW_REF = "flowRef";
  private static final String API_ID_NAME = "apiId";
  private static final String API_ID_VALUE = "[replace your api id here]";
  private static final String CREATE = "create";
  private static final String DOC_NAME = "name";

  public ApiMigrationStep() {
    this.setNamespacesContributions(asList(MULE_3_GATEWAY_NAMESPACE));
    this.setAppliedTo(getXPathSelector(MULE_3_GATEWAY_NAMESPACE, MULE_3_TAG_NAME));
  }

  @Override
  public String getDescription() {
    return "Update the api mappings to auto-discovery";
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    element.setName(MULE_4_TAG_NAME);
    element.setNamespace(MULE_4_GATEWAY_NAMESPACE);
    checkNotCreateTrue(element, report);
    element.removeContent();
    checkValueExists(element, report, FLOW_REF, "the attribute flowRef must be defined");
    element.setAttributes(
                          element.getAttributes().stream()
                              .filter(attribute -> attribute.getName().equals(FLOW_REF) ||
                                  attribute.getName().equals(DOC_NAME) &&
                                      attribute.getNamespace().equals(MULE_DOC_NAMESPACE))
                              .collect(Collectors.toList()));
    element.setAttribute(API_ID_NAME, API_ID_VALUE);
    report.report(WARN, element, element, "The attribute apiId must be set, obtain it from api management in the platform."); // TODO add link to reference material

  }

  private void checkNotCreateTrue(Element element, MigrationReport report) {
    ofNullable(element.getAttribute(CREATE)).ifPresent(attribute -> {
      try {
        if (attribute.getBooleanValue()) {
          report.report(WARN, element, element,
                        "The auto create functionality is no longer supported, create the api in platform to obtain the id."); // TODO add link to reference material
        }
      } catch (DataConversionException e) {
        report.report(WARN, element, element,
                      "Invalid value in create field, nevertheless this field is deprecated."); // TODO add link to reference material
      }
    });
  }

  private void checkValueExists(Element element, MigrationReport report, String name, String errorMessage, String... refer) {
    if (element.getAttribute(name) == null) {
      report.report(WARN, element, element, errorMessage, refer);
    }
  }

}
