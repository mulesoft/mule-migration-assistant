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

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

/**
 * expression-component processor migration strategy
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ExpressionComponent extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = getCoreXPathSelector("expression-component");

  @Override
  public String getDescription() {
    return "Mark expression-component processor as not supported for migration";
  }

  public ExpressionComponent() {
    this.setAppliedTo(XPATH_SELECTOR);
  }


  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    report.report("components.unsupported", object, object, "expression-component");
  }
}
