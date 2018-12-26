/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.mule.steps.file;

import static com.mulesoft.tools.migration.library.mule.steps.file.FileConfig.FILE_NAMESPACE_URI;

import com.mulesoft.tools.migration.step.AbstractApplicationModelMigrationStep;
import com.mulesoft.tools.migration.step.ExpressionMigratorAware;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.ExpressionMigrator;

import org.jdom2.Element;

/**
 * Migrates the transformers of the file transport
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class FileTransformers extends AbstractApplicationModelMigrationStep
    implements ExpressionMigratorAware {

  public static final String XPATH_SELECTOR =
      "//*[namespace-uri()='" + FILE_NAMESPACE_URI
          + "' and (local-name()='file-to-string-transformer' or local-name()='file-to-byte-array-transformer')]";

  private ExpressionMigrator expressionMigrator;

  @Override
  public String getDescription() {
    return "Update File transformers.";
  }

  public FileTransformers() {
    this.setAppliedTo(XPATH_SELECTOR);
  }

  @Override
  public void execute(Element object, MigrationReport report) throws RuntimeException {
    if (object.getAttribute("name") != null) {
      getApplicationModel().getNodes("//mule:transformer[@ref = '" + object.getAttributeValue("name") + "']")
          .forEach(t -> t.detach());
    }

    report.report("file.notNeeded", object, object.getParentElement(), object.getName());

    object.detach();
  }

  @Override
  public void setExpressionMigrator(ExpressionMigrator expressionMigrator) {
    this.expressionMigrator = expressionMigrator;
  }

  @Override
  public ExpressionMigrator getExpressionMigrator() {
    return expressionMigrator;
  }

}
