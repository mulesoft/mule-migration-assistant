/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.proxy.raml;

import com.mulesoft.tools.migration.library.gateway.steps.GatewayMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Common stuff to migrate raml elements
 *
 * @author Mulesoft Inc.
 */
public abstract class RamlMigrationStep extends GatewayMigrationStep {

  protected static final String REST_VALIDATOR_XSI_SCHEMA_LOCATION_URI =
      "http://www.mulesoft.org/schema/mule/rest-validator";
  protected static final String REST_VALIDATOR_XSI_SCHEMA_LOCATION_XSD =
      "http://www.mulesoft.org/schema/mule/rest-validator/current/mule-rest-validator.xsd";

  protected static final String RAML = "raml";

  protected static final String CONFIG_REF_ATTR_NAME = "config-ref";
  protected static final String RAML_PROXY_CONFIG = "raml-proxy-config";
  protected static final String CONFIG_TAG_NAME = "config";

  private static final String QUERY_PARAMS_STRICT_VALIDATION_ATTR_NAME = "queryParamsStrictValidation";
  private static final String QUERY_PARAMS_STRICT_VALIDATION_ATTR_VALUE = "validation.strict.queryParams";
  private static final String HEADERS_STRICT_VALIDATION_ATTR_NAME = "headersStrictValidation";
  private static final String HEADERS_STRICT_VALIDATION_ATTR_VALUE = "validation.strict.headers";
  public static final String API_ATTR_NAME = "api";
  public static final String IMPLEMENTATION_API_SPEC_ATTR_VALUE = "implementation.api.spec";
  public static final String PARSER_ATTR_VALUE = "parser";
  public static final String IMPLEMENTATION_API_PARSER_ATTR_VALUE = "implementation.api.parser";
  private static final String DISABLE_VALIDATIONS_ATTR_NAME = "disableValidations";
  private static final String DISABLE_VALIDATIONS_ATTR_VALUE = "validation.disable";

  public RamlMigrationStep(Namespace namespace, String tagName) {
    super(namespace, tagName);
  }

  protected void migrateConfigAttributes(Element ramlProxyConfigElement, MigrationReport migrationReport) {
    if (ramlProxyConfigElement.getAttribute(RAML) != null) {
      ramlProxyConfigElement.removeAttribute(RAML);
    }
    ramlProxyConfigElement.setAttribute(API_ATTR_NAME, "${" + IMPLEMENTATION_API_SPEC_ATTR_VALUE + "}");
    ramlProxyConfigElement.setAttribute(DISABLE_VALIDATIONS_ATTR_NAME, "${" + DISABLE_VALIDATIONS_ATTR_VALUE + "}");
    setNewConfigAttributes(ramlProxyConfigElement, migrationReport);
  }

  private void setNewConfigAttributes(Element ramlProxyConfig, MigrationReport migrationReport) {
    setMissingConfigAttribute(ramlProxyConfig, migrationReport, PARSER_ATTR_VALUE,
                              IMPLEMENTATION_API_PARSER_ATTR_VALUE);
    setMissingConfigAttribute(ramlProxyConfig, migrationReport, QUERY_PARAMS_STRICT_VALIDATION_ATTR_NAME,
                              QUERY_PARAMS_STRICT_VALIDATION_ATTR_VALUE);
    setMissingConfigAttribute(ramlProxyConfig, migrationReport, HEADERS_STRICT_VALIDATION_ATTR_NAME,
                              HEADERS_STRICT_VALIDATION_ATTR_VALUE);
  }

  private void setMissingConfigAttribute(Element ramlProxyConfig, MigrationReport migrationReport, String attributeName,
                                         String attributeValue) {
    ramlProxyConfig.setAttribute(attributeName, "${" + attributeValue + "}");
    migrationReport.report("raml.autocompletedConfigAttribute", ramlProxyConfig, ramlProxyConfig, attributeName, attributeValue);
  }

}
