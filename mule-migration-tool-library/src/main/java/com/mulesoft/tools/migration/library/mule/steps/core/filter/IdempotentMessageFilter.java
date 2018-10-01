/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.core.filter;

import com.mulesoft.tools.migration.step.category.MigrationReport;

import org.jdom2.Element;

/**
 * Migrate Idempotent Message Filter to Idempotent-Message Validator
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class IdempotentMessageFilter extends AbstractFilterMigrator {

  public static final String XPATH_SELECTOR = "//*[local-name()='idempotent-message-filter']";

  @Override
  public String getDescription() {
    return "Update Idempotent Message Filter to Idempotent-Message Validator.";
  }

  public IdempotentMessageFilter() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    element.setName("idempotent-message-validator");
  }

}
