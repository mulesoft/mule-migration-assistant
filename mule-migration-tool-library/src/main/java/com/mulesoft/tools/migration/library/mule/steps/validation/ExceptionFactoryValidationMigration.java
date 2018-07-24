/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.validation;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import org.jdom2.Element;

/**
 * Log error on report showing Exception Factory is no longer supported
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class ExceptionFactoryValidationMigration extends AbstractApplicationModelMigrationStep {

  private static final String VALIDATION_NAMESPACE_URI = "http://www.mulesoft.org/schema/mule/validation";
  public static final String XPATH_SELECTOR = "//*[namespace-uri()='" + VALIDATION_NAMESPACE_URI + "'"
      + " and local-name()='exception-factory']";

  @Override
  public String getDescription() {
    return "Migrate Custom Validation.";
  }

  public ExceptionFactoryValidationMigration() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element element, MigrationReport report) throws RuntimeException {
    report.report(MigrationReport.Level.ERROR, element, element,
                  "Exception Factory is no longer supported.",
                  "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-module-validation");
  }


}
