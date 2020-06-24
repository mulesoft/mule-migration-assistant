/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
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
