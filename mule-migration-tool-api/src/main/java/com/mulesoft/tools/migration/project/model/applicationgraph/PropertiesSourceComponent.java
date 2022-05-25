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
import java.util.Map;

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

  private final SourceType type;
  private FlowComponent responseComponent;

  public PropertiesSourceComponent(Element xmlElement, Flow parentFlow, ApplicationGraph applicationGraph) {
    super(xmlElement, parentFlow, applicationGraph);
    this.type = new SourceType(xmlElement.getNamespaceURI(), xmlElement.getName());
    Element responseComponent = getElementResponse(xmlElement);
    if (responseComponent != null) {
      this.responseComponent = new MessageProcessor(responseComponent, parentFlow, applicationGraph);
    }
  }

  private Element getElementResponse(Element xmlElement) {
    List<Element> detectedResponseElements =
        XmlDslUtils.getChildrenMatchingExpression(xmlElement, RESPONSE_BUILDER_EXPRESSION, Filters.element());
    if (!detectedResponseElements.isEmpty()) {
      return Iterables.getOnlyElement(detectedResponseElements);
    }
    return null;
  }

  @Override
  public SourceType getType() {
    return type;
  }

  public FlowComponent getResponseComponent() {
    return responseComponent;
  }
}
