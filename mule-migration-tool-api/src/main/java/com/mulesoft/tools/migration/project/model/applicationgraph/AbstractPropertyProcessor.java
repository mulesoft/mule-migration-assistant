/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.project.model.applicationgraph;

import org.jdom2.Element;

import java.util.regex.Pattern;

/**
 * Abstract class used in both copy-properties and remove properties that allows expressions
 *
 * @author Mulesoft Inc.
 * @since 1.4.0
 */
public abstract class AbstractPropertyProcessor extends MessageProcessor {

  private final Pattern expression;

  public AbstractPropertyProcessor(Element xmlElement, Flow parentFLow,
                                   ApplicationGraph graph) {
    super(xmlElement, parentFLow, graph);
    this.expression = Pattern.compile(translate(xmlElement.getAttribute("propertyName").getValue()));
  }

  protected String translate(String propertyExpression) {
    if (propertyExpression.endsWith("*")) {
      propertyExpression = propertyExpression.replace("*", ".*");
    } ;

    return propertyExpression;
  }

  public Pattern getExpression() {
    return this.expression;
  }

}
