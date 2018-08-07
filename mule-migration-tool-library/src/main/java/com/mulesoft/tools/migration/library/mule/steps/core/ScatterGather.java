/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.step.util.XmlDslUtils;
import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.ERROR;
import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;
import static com.mulesoft.tools.migration.step.util.TransportsUtils.COMPATIBILITY_NAMESPACE;

/**
 * Migrate Scatter Gather
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ScatterGather extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = "//mule:scatter-gather";

  @Override
  public String getDescription() {
    return "Migrate Scatter Gather.";
  }

  public ScatterGather() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    List<Element> childs = new ArrayList<>(element.getChildren());
    childs.forEach(c -> {
      if (c.getName().equals("processor-chain")) {
        c.setName("route");
      } else if (c.getName().equals("threading-profile")) {
        report.report(WARN, c, c, "Threading Profile no longer needed in Mule 4.",
                      "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-core");
        c.detach();
      } else if (c.getName().equals("custom-aggregation-strategy")) {
        report.report(ERROR, c, c, "Custom Aggregations are no longer supported.",
                      "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-core");
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
