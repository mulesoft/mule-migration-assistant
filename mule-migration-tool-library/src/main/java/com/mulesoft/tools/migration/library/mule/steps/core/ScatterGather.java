/*
 * Copyright (c) 2020 MuleSoft, Inc. This software is protected under international
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
 * Migrate Scatter Gather
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ScatterGather extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "//mule:*[local-name()='scatter-gather' or local-name()='all']";

  @Override
  public String getDescription() {
    return "Migrate Scatter Gather.";
  }

  public ScatterGather() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {

    if (element.getName().equals("all")) {
      element.setName("scatter-gather");
    }

    List<Element> childs = new ArrayList<>(element.getChildren());
    childs.forEach(c -> {
      if (c.getName().equals("processor-chain")) {
        c.setName("route");
      } else if (c.getName().equals("threading-profile")) {
        report.report("scatterGather.threading", c, element);
        c.detach();
      } else if (c.getName().equals("custom-aggregation-strategy")) {
        report.report("scatterGather.customAggregation", c, element);
      } else {
        Element newRouteElement = new Element("route", element.getNamespace());
        Integer childIndex = element.indexOf(c);
        c.detach();
        newRouteElement.addContent(c);
        element.addContent(childIndex, newRouteElement);
      }
    });
  }
}
