/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.engine;

import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.report.ReportingStrategy;
import com.mulesoft.tools.migration.report.console.ConsoleReportStrategy;

/**
 * Basic unit of execution
 * 
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public abstract class MigrationStep implements Executable {

  private String stepDescriptor;

  private ApplicationModel applicationModel;

  // private Document document;
  // private List<Element> nodes;

  // private Boolean onErrorStop;
  private ReportingStrategy reportingStrategy;

  public void setStepDescriptor(String descriptor) {
    this.stepDescriptor = descriptor;
  }

  public String getStepDescriptor() {
    return this.stepDescriptor;
  }

  // public void setNodes(List<Element> nodes) {
  // this.nodes = nodes;
  // }
  //
  // public void setDocument(Document document) {
  // this.document = document;
  // }
  @Deprecated
  public List<Element> getNodes() {
    return null;
  }

  @Deprecated
  public Document getDocument() {
    return null;
  }

  public ApplicationModel getApplicationModel() {
    return applicationModel;
  }

  public ReportingStrategy getReportingStrategy() {
    if (null == this.reportingStrategy) {
      this.reportingStrategy = new ConsoleReportStrategy();
    }
    return reportingStrategy;
  }

  public void setReportingStrategy(ReportingStrategy reportingStrategy) {
    this.reportingStrategy = reportingStrategy;
  }

  // public Boolean getOnErrorStop() {
  // return onErrorStop;
  // }
  //
  // public void setOnErrorStop(Boolean onErrorStop) {
  // this.onErrorStop = onErrorStop;
  // }

}
