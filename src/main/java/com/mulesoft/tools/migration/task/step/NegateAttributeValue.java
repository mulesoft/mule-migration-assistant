/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.tools.migration.task.step;

import com.mulesoft.tools.migration.exception.MigrationStepException;
import org.jdom2.Attribute;
import org.jdom2.Element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mulesoft.tools.migration.report.ReportCategory.RULE_APPLIED;

public class NegateAttributeValue extends MigrationStep {

    private String attributeName;

    public NegateAttributeValue(String attributeName) {
        setAttributeName(attributeName);
    }

    public NegateAttributeValue(){}

    public void execute() throws Exception {
        Attribute att;
        try {
            if (this.getAttributeName() != null) {
                for (Element node : getNodes()) {
                    att = node.getAttribute(this.getAttributeName());
                    if (att != null) {
                        String attValue = att.getValue();
                        Pattern pattern = Pattern.compile("#\\[(.*?)\\]");
                        Matcher matcher = pattern.matcher(attValue);
                        if (matcher.find()) {
                            att.setValue("#[not(" + matcher.group(1) + ")]");
                        } else {
                            att.setValue("#[not(" + attValue + ")]");
                        }

                        getReportingStrategy().log("Attribute negated:" + att, RULE_APPLIED, this.getDocument().getBaseURI(), null , this);
                    }
                }
            }
        } catch (Exception ex) {
            throw new MigrationStepException("Negate attribute exception. " + ex.getMessage());
        }
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }
}
