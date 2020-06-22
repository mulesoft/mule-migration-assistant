/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.COMPATIBILITY_NAMESPACE;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate <expression-transformer evaluator="attachments-list"/> to individual processors.
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class MessageAttachmentsListExpressionEvaluator extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "//mule:expression-transformer[@evaluator='attachments-list']";

  @Override
  public String getDescription() {
    return "Migrate <expression-transformer evaluator=\"attachments-list\"/> to individual processors.";
  }

  public MessageAttachmentsListExpressionEvaluator() {
    this.setAppliedTo(XPATH_SELECTOR);
    this.setNamespacesContributions(newArrayList(COMPATIBILITY_NAMESPACE));
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    object.setName("set-payload");
    object.removeAttribute("evaluator");

    String expression = "*";
    if (object.getAttribute("expression") != null) {
      expression = object.getAttributeValue("expression");
    }
    object.removeAttribute("expression");

    if ("*".equals(expression)) {
      object.setAttribute("value", "#[payload.attachments pluck ((value, key, index) -> value)]");
    } else {
      String asRegex = "^" + expression.replaceAll("\\*", ".*") + "$";
      object.setAttribute("value", "#[payload.attachments filterObject ((value,key) -> ((key as String) matches /" + asRegex
          + "/)) pluck ((value, key, index) -> value)]");
    }
  }
}
