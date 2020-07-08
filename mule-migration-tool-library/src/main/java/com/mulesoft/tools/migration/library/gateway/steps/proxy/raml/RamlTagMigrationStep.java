/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.proxy.raml;

import static com.mulesoft.tools.migration.library.gateway.steps.ElementFinder.containsElementWithMatchingAttributeValue;
import static com.mulesoft.tools.migration.library.gateway.steps.ElementFinder.findChildElement;
import static com.mulesoft.tools.migration.library.gateway.steps.ElementFinder.findChildElementWithMatchingAttributeValue;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.APIKIT_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.EE_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_CORE_NAMESPACE_NO_PREFIX;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.PROXY_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.REST_VALIDATOR_NAMESPACE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jdom2.Element;
import org.jdom2.Namespace;

import com.mulesoft.tools.migration.library.gateway.steps.GatewayMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

/**
 * Migrate raml tag
 *
 * @author Mulesoft Inc.
 */
public class RamlTagMigrationStep extends GatewayMigrationStep {

  private static final String EE_XSI_SCHEMA_LOCATION_URI =
      "http://www.mulesoft.org/schema/mule/ee/core http://www.mulesoft.org/schema/mule/ee/core/current/mule-ee.xsd";

  private static final String REST_VALIDATOR_XSI_SCHEMA_LOCATION_URI =
      "http://www.mulesoft.org/schema/mule/rest-validator";
  private static final String REST_VALIDATOR_XSI_SCHEMA_LOCATION_XSD =
      "http://www.mulesoft.org/schema/mule/rest-validator/current/mule-rest-validator.xsd";

  private static final String VALIDATE_REQUEST_TAG_NAME = "validate-request";

  private static final String REF_ATTR_NAME = "ref";
  private static final String ERROR_HANDLER_TAG_NAME = "error-handler";
  private static final String STATUS_CODE_ATTR_NAME = "statusCode";

  private static final String SET_PAYLOAD_VALUE = "output application/json --- {\"error\": \"$(error.description)\"}";

  private static final String ON_ERROR_CONTINUE = "on-error-continue";
  private static final String ON_ERROR_PROPAGATE_TAG_NAME = "on-error-propagate";
  private static final String TYPE_ATTR_NAME = "type";
  private static final String LOG_EXCEPTION_ATTR_NAME = "logException";
  private static final String FALSE_VALUE = "false";
  private static final String TRANSFORM_TAG_NAME = "transform";
  private static final String MESSAGE_TAG_NAME = "message";
  private static final String SET_PAYLOAD_TAG_NAME = "set-payload";
  private static final String SET_ATTRIBUTES_TAG_NAME = "set-attributes";

  private static final String RAML = "raml";

  private static final String CONFIG_REF_ATTR_NAME = "config-ref";
  private static final String RAML_PROXY_CONFIG = "raml-proxy-config";
  private static final String CONFIG_TAG_NAME = "config";

  private static final String QUERY_PARAMS_STRICT_VALIDATION_ATTR_NAME = "queryParamsStrictValidation";
  private static final String HEADERS_STRICT_VALIDATION_ATTR_NAME = "headersStrictValidation";
  private static final String API_ATTR_NAME = "api";
  private static final String PARSER_ATTR_VALUE = "parser";
  private static final String DEFAULT_IMPLEMENTATION_API_PARSER_ATTR_VALUE = "AUTO";
  private static final String DISABLE_VALIDATIONS_ATTR_NAME = "disableValidations";

  private static final Map<String, String> MAPPING_STATUS_CODES = new HashMap<String, String>() {

    {
      put("400", "REST-VALIDATOR:BAD_REQUEST");
      put("404", "REST-VALIDATOR:RESOURCE_NOT_FOUND");
      put("405", "REST-VALIDATOR:METHOD_NOT_ALLOWED");
      put("504", "HTTP:TIMEOUT");
    }
  };

  public RamlTagMigrationStep() {
    super(PROXY_NAMESPACE, RAML);
  }

  @Override
  public void execute(Element element, MigrationReport migrationReport) throws RuntimeException {
    element.setName(VALIDATE_REQUEST_TAG_NAME);
    element.setNamespace(REST_VALIDATOR_NAMESPACE);

    String configRef = element.getAttributeValue(CONFIG_REF_ATTR_NAME);

    Optional<Element> ramlProxyConfigOptional =
        findChildElementWithMatchingAttributeValue(getRootElement(element), RAML_PROXY_CONFIG,
                                                   PROXY_NAMESPACE, NAME_ATTR_NAME, configRef);
    if (ramlProxyConfigOptional.isPresent()) {
      Element ramlProxyConfig = ramlProxyConfigOptional.get();
      ramlProxyConfig.setName(CONFIG_TAG_NAME);
      ramlProxyConfig.setNamespace(REST_VALIDATOR_NAMESPACE);
      migrateConfigAttributes(ramlProxyConfig, migrationReport);
      migrateErrorHandlerElement(element.getParentElement(), migrationReport);
      addNamespaceDeclaration(getRootElement(element), REST_VALIDATOR_NAMESPACE,
                              REST_VALIDATOR_XSI_SCHEMA_LOCATION_URI + " " + REST_VALIDATOR_XSI_SCHEMA_LOCATION_XSD);
      addNamespaceDeclaration(getRootElement(element), EE_NAMESPACE, EE_XSI_SCHEMA_LOCATION_URI);
    } else {
      migrationReport.report("raml.noMatchingConfig", element, element);
      throw new RuntimeException("No matching config was found for RAML element.");
    }
  }

  private Element findApikitErrorHandler(Element flowElement) {
    Optional<Element> errorHandler =
        findChildElement(flowElement, ERROR_HANDLER_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX,
                         eHandler -> eHandler.getAttributeValue(REF_ATTR_NAME) != null);
    if (errorHandler.isPresent()) {
      String errorHandlerRef = errorHandler.get().getAttributeValue(REF_ATTR_NAME);
      return findChildElementWithMatchingAttributeValue(getRootElement(flowElement), ERROR_HANDLER_TAG_NAME,
                                                        MULE_4_CORE_NAMESPACE_NO_PREFIX, NAME_ATTR_NAME, errorHandlerRef)
                                                            .orElse(new Element(ERROR_HANDLER_TAG_NAME,
                                                                                MULE_4_CORE_NAMESPACE_NO_PREFIX));
    }
    return new Element(ERROR_HANDLER_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
  }

  private void migrateErrorHandlerElement(Element flowElement,
                                          MigrationReport migrationReport) {
    Element apikitErrorHandler = findApikitErrorHandler(flowElement);
    Element flowErrorHandler =
        findChildElementWithMatchingAttributeValue(flowElement, ERROR_HANDLER_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX,
                                                   REF_ATTR_NAME, apikitErrorHandler.getAttributeValue(NAME_ATTR_NAME))
                                                       .orElseGet(() -> getNewElement(flowElement, ERROR_HANDLER_TAG_NAME,
                                                                                      MULE_4_CORE_NAMESPACE_NO_PREFIX));
    flowErrorHandler.removeAttribute(REF_ATTR_NAME);
    convertOnErrorPropagatesToOnErrorContinue(apikitErrorHandler, flowErrorHandler, migrationReport);
    apikitErrorHandler.detach();
  }

  private Element getNewElement(Element parentElement, String tagName, Namespace namespace) {
    Element element = new Element(tagName, namespace);
    parentElement.addContent(element);
    return element;
  }

  private void convertOnErrorPropagatesToOnErrorContinue(Element apikitErrorHandlerElement, Element errorHandlerElement,
                                                         MigrationReport migrationReport) {
    List<Element> mappings =
        apikitErrorHandlerElement.getChildren(ON_ERROR_PROPAGATE_TAG_NAME, APIKIT_NAMESPACE);
    MAPPING_STATUS_CODES.keySet().forEach(statusCode -> {
      Element onErrorContinueElement =
          createOnErrorContinueElement(MAPPING_STATUS_CODES.get(statusCode), !statusCode.equals("504"), statusCode);
      errorHandlerElement.addContent(onErrorContinueElement);
      if (!containsElementWithMatchingAttributeValue(mappings, STATUS_CODE_ATTR_NAME, statusCode)) {
        migrationReport.report("raml.autocompletedOnErrorContinueElement", onErrorContinueElement, onErrorContinueElement,
                               MAPPING_STATUS_CODES.get(statusCode));
      }
    });
  }

  private Element createOnErrorContinueElement(String type, boolean hasPayload, String statusCode) {
    return new Element(ON_ERROR_CONTINUE, MULE_4_CORE_NAMESPACE_NO_PREFIX)
        .setAttribute(TYPE_ATTR_NAME, type)
        .setAttribute(LOG_EXCEPTION_ATTR_NAME, FALSE_VALUE)
        .addContent(new Element(TRANSFORM_TAG_NAME, EE_NAMESPACE).addContent(createMessageElement(hasPayload, statusCode)));
  }

  private Element createMessageElement(boolean hasPayload, String statusCode) {
    Element messageElement = new Element(MESSAGE_TAG_NAME, EE_NAMESPACE);
    if (hasPayload) {
      messageElement.addContent(new Element(SET_PAYLOAD_TAG_NAME, EE_NAMESPACE).addContent(SET_PAYLOAD_VALUE));
    }
    return messageElement.addContent(new Element(SET_ATTRIBUTES_TAG_NAME, EE_NAMESPACE)
        .addContent("{ " + STATUS_CODE_ATTR_NAME + ": " + statusCode + " }"));
  }

  private void migrateConfigAttributes(Element ramlProxyConfigElement, MigrationReport migrationReport) {
    ramlProxyConfigElement.setAttribute(API_ATTR_NAME, get3xAttributeValue(ramlProxyConfigElement, RAML));
    ramlProxyConfigElement.removeAttribute(RAML);
    ramlProxyConfigElement.setAttribute(DISABLE_VALIDATIONS_ATTR_NAME,
                                        get3xAttributeValue(ramlProxyConfigElement, DISABLE_VALIDATIONS_ATTR_NAME));
    setNewConfigAttributes(ramlProxyConfigElement, migrationReport);
  }

  private String get3xAttributeValue(Element ramlProxyConfigElement, String attributeName) {
    String attributeValue = ramlProxyConfigElement.getAttributeValue(attributeName);
    if (attributeValue.startsWith("![p['")) {
      attributeValue = "${" + attributeValue.substring(5, attributeValue.lastIndexOf("']]")) + "}";
    }
    return attributeValue;
  }

  private void setNewConfigAttributes(Element ramlProxyConfig, MigrationReport migrationReport) {
    setMissingConfigAttribute(ramlProxyConfig, migrationReport, PARSER_ATTR_VALUE,
                              DEFAULT_IMPLEMENTATION_API_PARSER_ATTR_VALUE);
    setMissingConfigAttribute(ramlProxyConfig, migrationReport, QUERY_PARAMS_STRICT_VALIDATION_ATTR_NAME,
                              FALSE_VALUE);
    setMissingConfigAttribute(ramlProxyConfig, migrationReport, HEADERS_STRICT_VALIDATION_ATTR_NAME,
                              FALSE_VALUE);
  }

  private void setMissingConfigAttribute(Element ramlProxyConfig, MigrationReport migrationReport, String attributeName,
                                         String attributeValue) {
    ramlProxyConfig.setAttribute(attributeName, attributeValue);
    migrationReport.report("raml.autocompletedConfigAttribute", ramlProxyConfig, ramlProxyConfig, attributeName, attributeValue);
  }

}
