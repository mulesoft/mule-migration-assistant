/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;

import org.jdom2.Document;

import com.mulesoft.tools.migration.engine.exception.MigrationTaskException;
import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.report.ReportingStrategy;
import com.mulesoft.tools.migration.report.console.ConsoleReportStrategy;

/**
 * A task is composed by one or more steps
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MigrationTask implements Executable {

  private Boolean onErrorStop;
  // TODO rename to description
  private String taskDescriptor;
  private ReportingStrategy reportingStrategy;
  private ArrayList<MigrationStep> migrationSteps;


  private ApplicationModel applicationModel;


  public void setApplicationModel(ApplicationModel applicationModel) {
    checkArgument(applicationModel != null, "The application model must not be null.");
    this.applicationModel = applicationModel;
  }

  public void setMigrationSteps(ArrayList<MigrationStep> migrationSteps) {
    checkArgument(migrationSteps != null, "The migration steps must not be null.");

    this.migrationSteps = migrationSteps;
  }

  public String getTaskDescriptor() {
    return taskDescriptor;
  }

  public void setTaskDescriptor(String taskDescriptor) {
    this.taskDescriptor = taskDescriptor;
  }

  public void setOnErrorStop(Boolean onErrorStop) {
    this.onErrorStop = onErrorStop;
  }

  public void setReportingStrategy(ReportingStrategy reportingStrategy) {
    this.reportingStrategy = reportingStrategy;
  }

  // TODO avoid this default it should come from another place (builder or ReportStrategyLocator)
  public ReportingStrategy getReportingStrategy() {
    if (null == this.reportingStrategy) {
      this.reportingStrategy = new ConsoleReportStrategy();
    }
    return reportingStrategy;
  }

  public void execute() throws Exception {
    checkState(applicationModel != null, "An application model must be provided.");

    try {
      for (MigrationStep step : migrationSteps) {
        // TODO fix this report
        // getReportingStrategy().log(step.getStepDescriptor(), TRYING_TO_APPLY, this.doc.getBaseURI(), this, null);

        step.setReportingStrategy(getReportingStrategy());
        step.setApplicationModel(applicationModel);
        step.execute();
      }
    } catch (Exception ex) {
      // TODO report this failure properly
      if (onErrorStop) {
        throw new MigrationTaskException("Task execution exception. " + ex.getMessage());
      }
    }
  }


  // public void execute() throws Exception {
  // try {
  // // TODO this is not needed anymore
  // // String xpathSelector = "";
  // // List<Element> nodes = getNodesFromXPath(xpathSelector);
  // // if (nodes.size() > 0) {
  // // TODO fix this report
  // // getReportingStrategy().log(this.xpathSelector + " (" + taskDescriptor + ")", WORKING_WITH_NODES, doc.getBaseURI(),
  // // this, null);
  // for (MigrationStep step : migrationSteps) {
  // step.setReportingStrategy(getReportingStrategy());
  // // step.setDocument(this.doc);
  // // step.setOnErrorStop(this.onErrorStop);
  // // step.setNodes(nodes);
  // getReportingStrategy().log(step.getStepDescriptor(), TRYING_TO_APPLY, this.doc.getBaseURI(), this, null);
  // step.execute();
  // }
  // // }
  // } catch (Exception ex) {
  // // TODO no no THROW A PROPER EXCEPTION
  // if (ex.getMessage().endsWith("has not been declared.")) {
  // // TODO fix this report
  // // getReportingStrategy().log("Task " + xpathSelector + " - " + ex.getMessage(), SKIPPED, doc.getBaseURI(), this, null);
  // } else {
  // // TODO fix this report
  // // getReportingStrategy().log("Executing the task for:" + xpathSelector + ":" + ex.getMessage(), ERROR,doc.getBaseURI(),
  // // this, null);
  // ex.printStackTrace();
  // }
  //
  // if (onErrorStop) {
  // throw new MigrationTaskException("Task execution exception. " + ex.getMessage());
  // }
  // }
  // }

  // private List<Element> getNodesFromXPath(String XpathExpression) {
  // if (!Strings.isNullOrEmpty(XpathExpression) && doc != null) {
  // XPathExpression<Element> xpath = XPathFactory.instance()
  // .compile(XpathExpression, Filters.element(), null, doc.getRootElement().getAdditionalNamespaces());
  // List<Element> nodes = xpath.evaluate(doc);
  // return nodes;
  // } else {
  // return Collections.emptyList();
  // }
  // }


  @Deprecated
  public void setDocument(Document document) {
    // this.doc = document;
  }

  @Deprecated
  public MigrationTask(String xpathSelector) {
    // this.xpathSelector = xpathSelector;
    this.migrationSteps = new ArrayList<>();
  }

  @Deprecated
  public MigrationTask() {
    this.migrationSteps = new ArrayList<>();
  }

  @Deprecated
  public void addStep(MigrationStep step) {
    if (step != null) {
      this.migrationSteps.add(step);
    }
  }
}
