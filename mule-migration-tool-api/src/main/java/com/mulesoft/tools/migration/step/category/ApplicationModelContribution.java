/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.step.category;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.MigrationStep;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.xpath.XPathExpression;

import java.util.List;

/**
 * Migration Step Interface that works over the application model. This should be used when migrating elements from the configuration files.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public interface ApplicationModelContribution extends MigrationStep<Element> {

  /**
   * Returns the {@link XPathExpression} that matches all elements on which this contribution should be applied to.
   *
   * @return a {@link XPathExpression}
   */
  XPathExpression getAppliedTo();

  /**
   * Sets the xpath expression that matches all elements on which this contribution should be applied to.
   *
   * @param xpathExpression the XPath expression
   */
  void setAppliedTo(String xpathExpression);

  /**
   * Retrieves the application model that this contribution step is working over.
   *
   * @return a {@link ApplicationModel}
   */
  ApplicationModel getApplicationModel();

  /**
   * Sets the application model on which this contribution step should work over.
   *
   * @return a {@link ApplicationModel}
   */
  void setApplicationModel(ApplicationModel appModel);

  /**
   * Retrieves the list of namespaces that this contribution step is providing.
   *
   * @return a {@link List<Namespace>}
   */
  List<Namespace> getNamespacesContributions();

  /**
   * Sets the namespaces that this contribution step is providing.
   *
   * @return a {@link ApplicationModel}
   */
  void setNamespacesContributions(List<Namespace> namespaces);
}
