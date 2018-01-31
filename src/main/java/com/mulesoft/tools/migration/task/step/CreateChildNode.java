/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.task.step;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;

import static com.mulesoft.tools.migration.report.ReportCategory.RULE_APPLIED;
import static com.mulesoft.tools.migration.report.ReportCategory.SKIPPED;

public class CreateChildNode extends MigrationStep {

    private String name;

    public CreateChildNode(String name) {
        setName(name);
    }

    public CreateChildNode(){}

    public void execute() throws Exception {
        try {
            if(!StringUtils.isBlank(name)) {
                for (Element node : getNodes()) {
                    if (node.getChild(getName(), node.getNamespace()) != null) {
                        getReportingStrategy().log("<" + node.getChild(getName(), node.getNamespace()).getQualifiedName() + "> node already exists.", SKIPPED, this.getDocument().getBaseURI(), null , this);
                    }
                    else {
                        Element child = new Element(getName());
                        child.setNamespace(node.getNamespace());
                        node.addContent(child);

                        getReportingStrategy().log("<" + child.getQualifiedName() + "> node was created. Namespace " + child.getNamespaceURI(), RULE_APPLIED, this.getDocument().getBaseURI(), null , this);
                    }
                }
            }
        } catch (Exception ex) {
            throw new MigrationStepException("Create child node exception. " + ex.getMessage());
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
