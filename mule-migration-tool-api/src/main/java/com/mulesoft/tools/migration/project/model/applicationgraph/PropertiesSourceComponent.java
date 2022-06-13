/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mulesoft.tools.migration.step.util.XmlDslUtils;
import org.jdom2.Element;
import org.jdom2.filter.Filters;

import java.util.List;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.*;

/**
 * Models a mule message source
 *
 * @author Mulesoft Inc.
 * @since 1.3.0
 */
public class PropertiesSourceComponent extends MessageProcessor implements PropertiesSource, FlowComponent {

  public static final String RESPONSE_BUILDER_EXPRESSION =
      getAllElementsFromNamespaceXpathSelector(HTTP_NAMESPACE.getURI(), ImmutableList.of("response-builder"), false, true);

  public static final String ERROR_RESPONSE_BUILDER_EXPRESSION =
      getAllElementsFromNamespaceXpathSelector(HTTP_NAMESPACE.getURI(), ImmutableList.of("error-response-builder"), false, true);

  private final SourceType type;
  private MessageProcessor responseComponent;
  private MessageProcessor errorResponseComponent;

  public PropertiesSourceComponent(Element xmlElement, SourceType type, Flow parentFlow, ApplicationGraph applicationGraph) {
    super(xmlElement, parentFlow, applicationGraph);
    this.type = type;
    Element responseComponent = getResponseElement(xmlElement);
    if (responseComponent != null) {
      this.responseComponent = new MessageProcessor(responseComponent, parentFlow, applicationGraph);
    } else {
      this.responseComponent = new SyntheticMessageProcessor(xmlElement, "_response", parentFlow, applicationGraph);
    }
    Element errorResponseComponent = getErrorResponseElement(xmlElement);
    if (errorResponseComponent != null) {
      this.errorResponseComponent = new MessageProcessor(errorResponseComponent, parentFlow, applicationGraph);
    } else {
      this.errorResponseComponent = new SyntheticMessageProcessor(xmlElement, "_errorResponse", parentFlow, applicationGraph);
    }
  }

  private Element getResponseElement(Element xmlElement) {
    return getChildElement(xmlElement, RESPONSE_BUILDER_EXPRESSION);
  }

  private Element getErrorResponseElement(Element xmlElement) {
    return getChildElement(xmlElement, ERROR_RESPONSE_BUILDER_EXPRESSION);
  }

  private Element getChildElement(Element xmlElement, String expression) {
    List<Element> detectedResponseElements =
        XmlDslUtils.getChildrenMatchingExpression(xmlElement, expression, Filters.element());
    if (!detectedResponseElements.isEmpty()) {
      return Iterables.getOnlyElement(detectedResponseElements);
    }
    return null;
  }

  @Override
  public SourceType getType() {
    return type;
  }

  public MessageProcessor getResponseComponent() {
    return responseComponent;
  }

  public MessageProcessor getErrorResponseComponent() {
    return errorResponseComponent;
  }

  @Override
  public void accept(FlowComponentVisitor visitor) {
    visitor.visitPropertiesSourceComponent(this, false);
  }

}
