/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.step;

import static com.google.common.base.Preconditions.checkArgument;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.ApplicationModelContribution;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  @Override
  public boolean shouldReportMetrics() {
    return true;
  }
}
