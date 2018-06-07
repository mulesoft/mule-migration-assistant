/*
 * Copyright (c) 2017 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.tools.migration.library.tools.mel;

import com.mulesoft.tools.migration.project.model.ApplicationModel;
import com.mulesoft.tools.migration.step.category.MigrationReport;
import com.mulesoft.tools.migration.util.CompatibilityResolver;
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
  public String resolve(String original, Element element, MigrationReport report, ApplicationModel model) {
    report.report(MigrationReport.Level.WARN, element, element,
                  "MEL expression could not be migrated to a DataWeave expression",
                  "https://docs.mulesoft.com/mule4-user-guide/v/4.1/migration-mel",
                  "https://blogs.mulesoft.com/dev/mule-dev/why-dataweave-main-expression-language-mule-4/");

    return "mel:" + original;
  }

  public static String resolveIdentifiers(String expression) {
    return expression.replaceAll("flowVars", "vars").replaceAll("message\\.inboundProperties",
                                                                "vars.compatibility_inboundProperties");
  }

}
