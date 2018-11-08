/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Migrate Until Successful
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class UntilSuccessful extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "//mule:*[local-name()='until-successful']";

  @Override
  public String getDescription() {
    return "Migrate Until Successful.";
  }

  public UntilSuccessful() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    List<Element> childs = new ArrayList<>(element.getChildren());
    childs.forEach(c -> {
      if (c.getName().equals("processor-chain")) {
        List<Element> chainNodes = new ArrayList<>(c.getChildren());
        chainNodes.forEach(n -> n.detach());
        element.addContent(element.indexOf(c), chainNodes);
        c.detach();
      } else if (c.getName().equals("threading-profile")) {
        report.report("untilSuccessful.threading", c, element);
        c.detach();
      }
    });

    if (element.getAttribute("ackExpression") != null) {
      element.removeAttribute("ackExpression");
    }

    if (element.getAttribute("deadLetterQueue-ref") != null) {
      element.removeAttribute("deadLetterQueue-ref");
    }

    if (element.getAttribute("failureExpression") != null) {
      element.removeAttribute("failureExpression");
    }

    if (element.getAttribute("objectStore-ref") != null) {
      element.removeAttribute("objectStore-ref");
    }

  }
}
