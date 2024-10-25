/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a Apache 2.0 License
 * license that can be found in the LICENSE.txt file.
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
