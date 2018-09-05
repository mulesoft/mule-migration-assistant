/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.tools.mel;

import static com.mulesoft.tools.migration.step.category.MigrationReport.Level.WARN;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.CompatibilityResolver;

import com.mulesoft.tools.migration.util.ExpressionMigrator;
import org.jdom2.Element;

/**
 * Default implementation to resolve compatibility issues with MEL
 *
 * @author Mulesoft Inc.
 * @since 1.0.0
 */
public class DefaultMelCompatibilityResolver implements CompatibilityResolver<String> {

  @Override
  public boolean canResolve(String original) {
    return true;
  }

  @Override
  public String resolve(String original, Element element, MigrationReport report, ApplicationModel model,
                        ExpressionMigrator expressionMigrator) {
    report.report(MigrationReport.Level.WARN, element, element,
                  "MEL expression could not be migrated to a DataWeave expression",
                  "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-mel",
                  "https://beta-migrator.docs-stgx.mulesoft.com/mule4-user-guide/v/4.1/migration-manual#mel_expressions",
                  "https://blogs.mulesoft.com/dev/mule-dev/why-dataweave-main-expression-language-mule-4/");

    return "mel:" + original;
  }

}
