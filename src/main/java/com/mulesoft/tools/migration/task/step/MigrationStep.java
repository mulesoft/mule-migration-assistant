/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.task.step;

import com.mulesoft.tools.migration.report.console.ConsoleReportStrategy;
import com.mulesoft.tools.migration.report.ReportingStrategy;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.List;

public abstract class MigrationStep {

    private List<Element> nodes;
    private Document document;
    private Boolean onErrorStop;
    private ReportingStrategy reportingStrategy;
    private String stepDescriptor;

    public void setStepDescriptor(String descriptor) {
        this.stepDescriptor = descriptor;
    }

    public String getStepDescriptor() {
        return this.stepDescriptor;
    }

    public void setNodes(List<Element> nodes) {
        this.nodes = nodes;
    }

    public List<Element> getNodes() {
        return this.nodes;
    }

    public void  setDocument(Document document) {
        this.document = document;
    }

    public Document getDocument() {
        return this.document;
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

    public Boolean getOnErrorStop() {
        return onErrorStop;
    }

    public void setOnErrorStop(Boolean onErrorStop) {
        this.onErrorStop = onErrorStop;
    }

    public abstract void execute() throws Exception;
}
