/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.library.gateway.steps.policy.throttling;

import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.HTTP_TRANSFORM_NAMESPACE;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.MULE_4_CORE_NAMESPACE_NO_PREFIX;
import static com.mulesoft.tools.migration.library.gateway.steps.GatewayNamespaces.THROTTLING_MULE_4_NAMESPACE;

import com.mulesoft.tools.migration.library.gateway.steps.policy.PolicyMigrationStep;

import java.util.List;

import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Common stuff to migrate throttling elements
 *
 * @author Mulesoft Inc.
 */
public abstract class AbstractThrottlingMigrationStep extends PolicyMigrationStep {

  protected static final String RATE_LIMIT_TAG_NAME = "rate-limit";

  protected static final String DELAY_RESPONSE_TAG_NAME = "delay-response";
  protected static final String DISCARD_RESPONSE_TAG_NAME = "discard-response";
  protected static final String THROTTLE_TAG_NAME = "throttle";

  protected static final String ON_ERROR_PROPAGATE_TAG_NAME = "on-error-propagate";

  protected static final String CONFIG_TAG_NAME = "config";
  protected static final String CLUSTERIZABLE_ATTR_NAME = "clusterizable";
  protected static final String TRUE = "true";
  protected static final String FALSE = "false";
  protected static final String TIER_PROVIDER_TAG_NAME = "tier-provider";

  protected static final String ADD_HEADERS_TAG_NAME = "add-headers";
  private static final String OUTPUT_TYPE_ATTR_NAME = "outputType";
  private static final String OUTPUT_TYPE_ATTR_VALUE = "response";

  protected static final String CONFIG_REF_ATTR_NAME = "config-ref";
  protected static final String TARGET_ATTR_NAME = "target";
  protected static final String TARGET_ATTR_VALUE = "throttlingResponse";

  protected static final String HEADERS_TAG_NAME = "headers";
  private static final String DW_HEADERS_RESPONSE_VALUE =
      "#[ { 'x-ratelimit-remaining': vars.throttlingResponse.availableQuota as String, 'x-ratelimit-limit': vars.throttlingResponse.maximumAllowedRequests as String, 'x-ratelimit-reset': vars.throttlingResponse.remainingFrame as String } ]";

  private static final String THROTTLING_XSI_SCHEMA_LOCATION_URI =
      "http://www.mulesoft.org/schema/mule/throttling http://www.mulesoft.org/schema/mule/throttling/current/mule-throttling.xsd";
  private static final String HTTP_TRANSFORM_XSI_SCHEMA_LOCATION_URI =
      "http://www.mulesoft.org/schema/mule/http-policy-transform http://www.mulesoft.org/schema/mule/http-policy-transform/current/mule-http-policy-transform.xsd";

  public AbstractThrottlingMigrationStep(final Namespace namespace, final String tagName) {
    super(namespace, tagName);
  }

  private Element getHeadersElement(String dwResponseValue) {
    return new Element(HEADERS_TAG_NAME, HTTP_TRANSFORM_NAMESPACE).addContent(dwResponseValue);
  }

  protected Element getAddHeadersElement() {
    return new Element(ADD_HEADERS_TAG_NAME, HTTP_TRANSFORM_NAMESPACE).setAttribute(OUTPUT_TYPE_ATTR_NAME, OUTPUT_TYPE_ATTR_VALUE)
        .addContent(getHeadersElement(DW_HEADERS_RESPONSE_VALUE));
  }

  protected Element getOnErrorPropagateElement() {
    return new Element(ON_ERROR_PROPAGATE_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX)
        .addContent(getAddHeadersElement());
  }

  private boolean matchesAddHeadersElement(Element existingAddHeadersElement, Element addHeadersElementToAdd) {
    return existingAddHeadersElement.getContentSize() == addHeadersElementToAdd.getContentSize()
        && existingAddHeadersElement.getAttributeValue(OUTPUT_TYPE_ATTR_NAME)
            .equals(addHeadersElementToAdd.getAttributeValue(OUTPUT_TYPE_ATTR_NAME))
        && existingAddHeadersElement.getChild(HEADERS_TAG_NAME, HTTP_TRANSFORM_NAMESPACE).getContent(0).getValue()
            .equals(addHeadersElementToAdd.getChild(HEADERS_TAG_NAME, HTTP_TRANSFORM_NAMESPACE).getContent(0).getValue());
  }

  protected void addAddHeadersElement(Element tryElement) {
    Element existingAddHeadersElement = tryElement.getChild(ADD_HEADERS_TAG_NAME, HTTP_TRANSFORM_NAMESPACE);
    Element addHeadersElementToAdd = getAddHeadersElement();
    if (existingAddHeadersElement == null || !matchesAddHeadersElement(existingAddHeadersElement, addHeadersElementToAdd))
      tryElement.addContent(getElementPosition(tryElement, EXECUTE_NEXT_TAG_NAME), addHeadersElementToAdd);
  }

  protected void addOnErrorPropagateElement(Element errorHandlerElement) {
    List<Element> onErrorPropagateElements =
        errorHandlerElement.getChildren(ON_ERROR_PROPAGATE_TAG_NAME, MULE_4_CORE_NAMESPACE_NO_PREFIX);
    Element onErrorPropagateElement = getOnErrorPropagateElement();
    if (onErrorPropagateElements.stream().filter(e -> {
      Element addHeadersChildElement = e.getChild(ADD_HEADERS_TAG_NAME, HTTP_TRANSFORM_NAMESPACE);
      return e.getContentSize() == onErrorPropagateElement.getContentSize() && addHeadersChildElement != null
          && matchesAddHeadersElement(addHeadersChildElement,
                                      onErrorPropagateElement.getChild(ADD_HEADERS_TAG_NAME, HTTP_TRANSFORM_NAMESPACE));
    }).findAny().orElse(null) == null) {
      errorHandlerElement.addContent(onErrorPropagateElement);
    }

  }

  protected void addNamespaceDeclarations(Element element) {
    Element rootElement = getRootElement(element);
    addNamespaceDeclaration(rootElement, THROTTLING_MULE_4_NAMESPACE, THROTTLING_XSI_SCHEMA_LOCATION_URI);
    addNamespaceDeclaration(rootElement, HTTP_TRANSFORM_NAMESPACE, HTTP_TRANSFORM_XSI_SCHEMA_LOCATION_URI);
  }

}
