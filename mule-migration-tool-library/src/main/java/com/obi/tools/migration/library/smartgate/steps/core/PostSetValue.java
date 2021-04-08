/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.obi.tools.migration.library.smartgate.steps.core;

import static com.mulesoft.tools.migration.step.util.XmlDslUtils.getCoreXPathSelector;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Post Migrate set-variable "smartgastae_transaction_id"
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class PostSetValue extends AbstractApplicationModelMigrationStep {

  public static final String XPATH_SELECTOR = getCoreXPathSelector("set-variable");

  @Override
  public String getDescription() {
    return "Post Migrate set-variable \"smartgastae_transaction_id\"";
  }

  public PostSetValue() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {

    if ("smartgate_transaction_id".equals(element.getAttributeValue("variableName"))
        && "#[vars.correlationId]".equals(element.getAttributeValue("value"))) {
      element.setAttribute("value", "#[correlationId]");
    }
  }
}
