/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.step.category;

import com.mulesoft.tools.migration.step.MigrationStep;
import org.jdom2.Element;
import org.jdom2.xpath.XPathExpression;

/**
 * Migration Step that works over the application model
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public interface ApplicationModelContribution extends MigrationStep<Element> {

  XPathExpression getAppliedTo();

  void setAppliedTo(String xpathExpression);
}
