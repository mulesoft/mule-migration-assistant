/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.task;

import static com.mulesoft.tools.migration.report.ReportCategory.ERROR;
import static com.mulesoft.tools.migration.report.ReportCategory.SKIPPED;
import static com.mulesoft.tools.migration.report.ReportCategory.TRYING_TO_APPLY;
import static com.mulesoft.tools.migration.report.ReportCategory.WORKING_WITH_NODES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import com.google.common.base.Strings;
import com.mulesoft.tools.migration.engine.exception.MigrationTaskException;
import com.mulesoft.tools.migration.report.ReportingStrategy;
import com.mulesoft.tools.migration.report.console.ConsoleReportStrategy;
import com.mulesoft.tools.migration.task.step.MigrationStep;

/**
 * A task is composed by one or more steps
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MigrationTask {

  private String xpathSelector;

  private Document doc;
  private List<Element> nodes;
  private String taskDescriptor;

  private Boolean onErrorStop;

  private ArrayList<MigrationStep> steps;
  private ReportingStrategy reportingStrategy;

  public MigrationTask(String xpathSelector) {
    this.xpathSelector = xpathSelector;
    this.steps = new ArrayList<>();
  }

  public MigrationTask() {
    this.steps = new ArrayList<>();
  }

  public void addStep(MigrationStep step) {
    if (step != null) {
      this.steps.add(step);
    }
  }

  public void execute() throws Exception {
    try {
      if (null == reportingStrategy) {
        reportingStrategy = new ConsoleReportStrategy();
      }
      nodes = getNodesFromXPath(this.xpathSelector);
      if (nodes.size() > 0) {
        getReportingStrategy().log(this.xpathSelector + " (" + this.taskDescriptor + ")", WORKING_WITH_NODES,
                                   this.doc.getBaseURI(), this, null);
        for (MigrationStep step : steps) {
          step.setReportingStrategy(this.reportingStrategy);
          step.setDocument(this.doc);
          step.setOnErrorStop(this.onErrorStop);
          step.setNodes(nodes);
          getReportingStrategy().log(step.getStepDescriptor(), TRYING_TO_APPLY, this.doc.getBaseURI(), this, null);
          step.execute();
        }
      }
    } catch (Exception ex) {
      if (ex.getMessage().endsWith("has not been declared.")) {
        getReportingStrategy().log("Task " + this.xpathSelector + " - " + ex.getMessage(), SKIPPED, this.doc.getBaseURI(), this,
                                   null);
      } else {
        getReportingStrategy().log("Executing the task for:" + this.xpathSelector + ":" + ex.getMessage(), ERROR,
                                   this.doc.getBaseURI(), this, null);
        ex.printStackTrace();
      }
      if (onErrorStop) {
        throw new MigrationTaskException("Task execution exception. " + ex.getMessage());
      }
    }
  }

  private List<Element> getNodesFromXPath(String XpathExpression) {
    if (!Strings.isNullOrEmpty(XpathExpression) && doc != null) {
      XPathExpression<Element> xpath = XPathFactory.instance()
          .compile(XpathExpression, Filters.element(), null, doc.getRootElement().getAdditionalNamespaces());
      List<Element> nodes = xpath.evaluate(doc);
      return nodes;
    } else {
      return Collections.emptyList();
    }
  }

  public void setTaskDescriptor(String descriptor) {
    this.taskDescriptor = descriptor;
  }

  public String getTaskDescriptor() {
    return this.taskDescriptor;
  }

  public void setDocument(Document document) {
    this.doc = document;
  }

  public void setReportingStrategy(ReportingStrategy reportingStrategy) {
    this.reportingStrategy = reportingStrategy;
  }

  public void setOnErrorStop(Boolean onErrorStop) {
    this.onErrorStop = onErrorStop;
  }

  public ReportingStrategy getReportingStrategy() {
    if (null == this.reportingStrategy) {
      this.reportingStrategy = new ConsoleReportStrategy();
    }
    return reportingStrategy;
  }
}
