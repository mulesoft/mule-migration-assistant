/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.step;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.ApplicationModelContribution;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import java.util.List;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.xpath.XPathExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper of ApplicationModelContribution steps that enables detailed reporting
 * of the element migration result.
 *
 * @author Mulesoft Inc.
 */
public class ReportingStep implements ApplicationModelContribution, ExpressionMigratorAware {

  private static final Logger logger = LoggerFactory.getLogger(ReportingStep.class);

  private final ApplicationModelContribution targetStep;

  public ReportingStep(ApplicationModelContribution step) {
    targetStep = step;
  }

  /**
   * Wraps the step execution in order to report the element migration result
   *
   *    success:
   *         - report error count not increased OR
   *         - report error count increased same amount as mel/dw errors
   *    failure:
   *         - report error count > mel/dw errors OR
   *         - exception thrown
   */
  @Override
  public void execute(Element element, MigrationReport report) {
    int entriesBefore = report.getReportEntries(ERROR).size();
    int melFailuresBefore = report.getMelExpressionsFailureCount();
    int dwFailuresBefore = report.getDwTransformsFailureCount();
    try {
      targetStep.execute(element, report);
      if (targetStep.shouldReportMetrics()) {
        if (report.getReportEntries(ERROR).size() <= entriesBefore + (report.getMelExpressionsFailureCount() - melFailuresBefore)
            + (report.getDwTransformsFailureCount() - dwFailuresBefore)) {
          report.addComponentSuccess(element);
        } else {
          report.addComponentFailure(element);
        }
      }
    } catch (Exception e) {
      logger.warn("Exception {} -- migrating {}:{}", e, element != null ? element.getNamespacePrefix() : "null",
                  element != null ? element.getName() : "null");
      if (targetStep.shouldReportMetrics()) {
        report.addComponentFailure(element);
      }
      throw e;
    }
  }

  @Override
  public String getDescription() {
    return targetStep.getDescription();
  }

  @Override
  public XPathExpression getAppliedTo() {
    return targetStep.getAppliedTo();
  }

  @Override
  public void setAppliedTo(String xpathExpression) {
    targetStep.setAppliedTo(xpathExpression);
  }

  @Override
  public ApplicationModel getApplicationModel() {
    return targetStep.getApplicationModel();
  }

  @Override
  public void setApplicationModel(ApplicationModel appModel) {
    targetStep.setApplicationModel(appModel);
  }

  @Override
  public List<Namespace> getNamespacesContributions() {
    return targetStep.getNamespacesContributions();
  }

  @Override
  public void setNamespacesContributions(List<Namespace> namespaces) {
    targetStep.setNamespacesContributions(namespaces);
  }

  @Override
  public boolean shouldReportMetrics() {
    return targetStep.shouldReportMetrics();
  }

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    if (targetStep instanceof ExpressionMigratorAware) {
      ((ExpressionMigratorAware) targetStep).setExpressionMigrator(expressionMigrator);
    }
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    if (targetStep instanceof ExpressionMigratorAware) {
      return ((ExpressionMigratorAware) targetStep).getExpressionMigrator();
    }
    return null;
  }
}
