/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.step;

import static com.google.common.base.Preconditions.checkArgument;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.ApplicationModelContribution;

import org.jdom2.Namespace;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic unit of execution.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class AbstractApplicationModelMigrationStep implements ApplicationModelContribution {

  private XPathExpression appliedTo;
  private ApplicationModel applicationModel;
  public List<Namespace> namespacesContribution = new ArrayList<>();

  @Override
  public XPathExpression getAppliedTo() {
    return appliedTo;
  }

  @Override
  public void setAppliedTo(String xpathExpression) {
    checkArgument(xpathExpression != null, "The xpath expression must not be null.");
    try {
      this.appliedTo = XPathFactory.instance().compile(xpathExpression);
    } catch (Exception ex) {
      throw new MigrationStepException("The xpath expression must be valid.", ex);
    }
  }

  @Override
  public ApplicationModel getApplicationModel() {
    return applicationModel;
  }

  @Override
  public void setApplicationModel(ApplicationModel applicationModel) {
    this.applicationModel = applicationModel;
  }

  @Override
  public String getDescription() {
    return "";
  }

  @Override
  public List<Namespace> getNamespacesContributions() {
    return namespacesContribution;
  }

  @Override
  public void setNamespacesContributions(List<Namespace> namespaces) {
    this.namespacesContribution = namespaces;
  }
}
