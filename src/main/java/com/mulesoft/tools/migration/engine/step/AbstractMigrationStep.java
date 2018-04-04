/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine.step;

import static com.google.common.base.Preconditions.checkArgument;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import org.jdom2.Element;

import java.util.List;

/**
 * Basic unit of execution
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class AbstractMigrationStep implements MigrationStep {

  private Element element;
  private String appliedTo;

  @Override
  public String getAppliedTo() {
    return appliedTo;
  }

  @Override
  public void setAppliedTo(String xpathExpression) {
    checkArgument(xpathExpression != null, "The xpath expression must not be null.");
    this.appliedTo = xpathExpression;
  }

  @Override
  public Element getElement() {
    return element;
  }

  @Override
  public void setElement(Element element) {
    checkArgument(element != null, "The element to execute step must not be null.");
    this.element = element;
  }
}
