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
 * Post Migrate set-variable "smartgate_transaction_id"
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class PostSetVariable extends AbstractApplicationModelMigrationStep {

  private static final String NULL = "null";
  private static final String VARS_CORRELATION_ID = "#[vars.correlationId]";
  private static final String CORRELATION_ID = "#[correlationId]";
  private static final String VALUE = "value";
  private static final String VARIABLE_NAME = "variableName";
  private static final String SMARTGATE_TRANSACTION_ID = "smartgate_transaction_id";
  private static final String MIMETYPE = "mimeType";
  public static final String XPATH_SELECTOR = getCoreXPathSelector("set-variable");

  @Override
  public String getDescription() {
    return "Post Migrate set-variable \"smartgate_transaction_id\"";
  }

  public PostSetVariable() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {

    if (SMARTGATE_TRANSACTION_ID.equals(element.getAttributeValue(VARIABLE_NAME))
        && VARS_CORRELATION_ID.equals(element.getAttributeValue(VALUE))) {
      element.setAttribute(VALUE, CORRELATION_ID);
    } else {
      final String attname = MIMETYPE;
      if (NULL.equals(element.getAttributeValue(attname))) {
        element.removeAttribute(MIMETYPE);
      }
    }
  }
}
