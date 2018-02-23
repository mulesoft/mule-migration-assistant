/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.task.step;

import com.mulesoft.tools.migration.engine.exception.MigrationStepException;
import org.jdom2.Attribute;
import org.jdom2.Element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mulesoft.tools.migration.report.ReportCategory.RULE_APPLIED;

/**
 * Negate attribute value
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class NegateAttributeValue extends MigrationStep {

  private String attributeName;

  public NegateAttributeValue(String attributeName) {
    setAttributeName(attributeName);
  }

  public NegateAttributeValue() {}

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

            getReportingStrategy().log("Attribute negated:" + att, RULE_APPLIED, this.getDocument().getBaseURI(), null, this);
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
