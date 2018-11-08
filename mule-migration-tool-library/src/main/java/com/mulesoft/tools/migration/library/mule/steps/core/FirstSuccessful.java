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
 * Migrate First Successful
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class FirstSuccessful extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "//mule:*[local-name()='first-successful']";

  @Override
  public String getDescription() {
    return "Migrate First Successful.";
  }

  public FirstSuccessful() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    List<Element> childs = new ArrayList<>(element.getChildren());
    childs.forEach(c -> {
      if (c.getName().equals("processor-chain")) {
        c.setName("route");
      } else {
        Element route = new Element("route", element.getNamespace());
        Integer index = element.indexOf(c);
        c.detach();
        route.addContent(c);
        element.addContent(index, route);
      }
    });

    if (element.getAttribute("failureExpression") != null) {
      element.removeAttribute("failureExpression");
    }
  }
}
